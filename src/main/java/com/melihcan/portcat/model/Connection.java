package com.melihcan.portcat.model;

import com.melihcan.portcat.service.PortResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

public class Connection {
    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private String remotePort;
    private String state;

    // Bekannte Server-Ports (Dienste die typischerweise Server sind)
    private static final Set<Integer> SERVER_PORTS = Set.of(
            20, 21,    // FTP
            22,        // SSH
            23,        // Telnet
            25,        // SMTP
            53,        // DNS
            80,        // HTTP
            110,       // POP3
            143,       // IMAP
            443,       // HTTPS
            993,       // IMAPS
            995,       // POP3S
            1433,      // MSSQL
            3306,      // MySQL
            3389,      // RDP
            5432,      // PostgreSQL
            5900,      // VNC
            6379,      // Redis
            8080,      // HTTP Alt
            8443,      // HTTPS Alt
            27017      // MongoDB
    );

    // Der Konstruktor
    public Connection(String localAddress, int localPort, String remoteAddress, String remotePort, String state) {
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.remoteAddress = remoteAddress + " " + resolveDns(remoteAddress);
        this.remotePort = PortResolver.resolvePort(remotePort); // Port auflösen!
        this.state = state;
    }

    // DNS-Auflösung
    private static String resolveDns(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            String hostname = address.getHostName();

            // Falls gleich wie IP, dann nicht auflösbar
            if (hostname.equals(ipAddress)) {
                return "";
            }
            return "(" + hostname + ")";
        } catch (UnknownHostException e) {
            return ""; // Falls Fehler, leerer String
        }
    }

    // Getter-Methoden
    public String getLocalAddress() { return localAddress; }
    public int getLocalPort() { return localPort; }
    public String getRemoteAddress() { return remoteAddress; }
    public String getRemotePort() { return remotePort; }
    public String getState() { return state; }

    /**
     * Bestimmt die Richtung der Verbindung:
     *
     * INCOMING (Eingehend):
     * - LISTEN: Lokaler Dienst wartet auf Verbindungen
     * - ESTABLISHED mit lokalem Server-Port: Jemand hat sich zu unserem Server verbunden
     *
     * OUTGOING (Ausgehend):
     * - ESTABLISHED mit remote Server-Port: Wir haben uns zu einem fremden Server verbunden
     *
     * UNKNOWN:
     * - Unbekannter State oder Port-Range macht keine eindeutige Zuordnung möglich
     */
    public String getDirection() {
        if (state == null) return "UNKNOWN";

        String stateUpper = state.toUpperCase();

        // 1. LISTEN = immer INCOMING (wir bieten einen Dienst an)
        if (stateUpper.contains("LISTEN")) {
            return "INCOMING";
        }

        // 2. ESTABLISHED = schaue welche Seite der Server ist
        if (stateUpper.contains("ESTABLISHED")) {
            try {
                // Remote-Port extrahieren (z.B. "443 (HTTPS)" → 443)
                String remotePortStr = remotePort.split(" ")[0].trim();
                int remotePortNum = Integer.parseInt(remotePortStr);

                // Wenn REMOTE-Port ein bekannter Server-Port ist
                // → WIR haben uns zu einem Server verbunden → OUTGOING
                if (SERVER_PORTS.contains(remotePortNum)) {
                    return "OUTGOING";
                }

                // Wenn LOCAL-Port ein bekannter Server-Port ist
                // → Jemand hat sich zu UNSEREM Server verbunden → INCOMING
                if (SERVER_PORTS.contains(localPort)) {
                    return "INCOMING";
                }

                // Heuristik: High Ports (>49152) sind meist Client-Ports
                // Wenn unser Port hoch ist und der Remote niedrig → OUTGOING
                if (localPort > 49152 && remotePortNum < 49152) {
                    return "OUTGOING";
                }

                // Wenn unser Port niedrig ist und der Remote hoch → INCOMING
                if (localPort < 49152 && remotePortNum > 49152) {
                    return "INCOMING";
                }

            } catch (Exception e) {
                // Fehler beim Parsen → UNKNOWN
            }
        }

        // Andere States (CLOSE-WAIT, TIME-WAIT, etc.) oder unklare Situation
        return "UNKNOWN";
    }

    @Override
    public String toString() {
        return "Connection [Local=" + localAddress + ":" + localPort +
                ", Remote=" + remoteAddress + ":" + remotePort + ", State=" + state + "]";
    }
}