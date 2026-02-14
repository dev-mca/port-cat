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
            445,       // SMB (NEU)
            465,       // SMTPS (NEU)
            587,       // SMTP Submission (NEU)
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
            8888,      // HTTP Alt (NEU)
            27017      // MongoDB
    );

    /**
     * Constructor MIT optionalem DNS-Lookup
     *
     * @param localAddress Lokale IP-Adresse
     * @param localPort Lokaler Port
     * @param remoteAddress Remote IP-Adresse
     * @param remotePort Remote Port
     * @param state Verbindungsstatus
     * @param resolveDns TRUE = DNS-Lookup durchführen (LANGSAM!), FALSE = nur IP zeigen
     */
    public Connection(String localAddress, int localPort, String remoteAddress, String remotePort, String state, boolean resolveDns) {
        this.localAddress = localAddress;
        this.localPort = localPort;

        // DNS-Lookup nur wenn explizit gewünscht
        if (resolveDns) {
            this.remoteAddress = remoteAddress + " " + resolveDnsLookup(remoteAddress);
        } else {
            this.remoteAddress = remoteAddress;
        }

        this.remotePort = PortResolver.resolvePort(remotePort);
        this.state = state;
    }

    /**
     * Legacy Constructor (ohne DNS - schnell!)
     * Wird verwendet wenn DNS-Toggle AUS ist
     */
    public Connection(String localAddress, int localPort, String remoteAddress, String remotePort, String state) {
        this(localAddress, localPort, remoteAddress, remotePort, state, false);
    }

    /**
     * Führt DNS-Lookup durch
     * ACHTUNG: Kann 0.5-3 Sekunden dauern!
     */
    private static String resolveDnsLookup(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            String hostname = address.getHostName();

            if (hostname.equals(ipAddress)) {
                return "";
            }
            return "(" + hostname + ")";
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public String getLocalAddress() { return localAddress; }
    public int getLocalPort() { return localPort; }
    public String getRemoteAddress() { return remoteAddress; }
    public String getRemotePort() { return remotePort; }
    public String getState() { return state; }

    /**
     * Bestimmt die Richtung der Verbindung:
     *
     * INCOMING: LISTEN oder ESTABLISHED mit lokalem Server-Port
     * OUTGOING: ESTABLISHED mit remote Server-Port
     * UNKNOWN: Nicht eindeutig zuordenbar
     */
    public String getDirection() {
        if (state == null || state.trim().isEmpty()) {
            return "UNKNOWN";
        }

        String stateUpper = state.toUpperCase().trim();

        // 1. LISTEN = immer INCOMING
        if (stateUpper.contains("LISTEN")) {
            return "INCOMING";
        }

        // 2. ESTABLISHED = schaue welche Seite der Server ist
        if (stateUpper.contains("ESTABLISHED")) {
            try {
                // Remote-Port extrahieren (z.B. "443 (HTTPS)" → "443")
                String remotePortStr = remotePort;
                if (remotePortStr == null) return "UNKNOWN";

                if (remotePortStr.contains(" ")) {
                    remotePortStr = remotePortStr.split(" ")[0];
                }
                if (remotePortStr.contains("(")) {
                    remotePortStr = remotePortStr.split("\\(")[0].trim();
                }

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
                if (localPort > 49152 && remotePortNum < 49152) {
                    return "OUTGOING";
                }

                if (localPort < 49152 && remotePortNum > 49152) {
                    return "INCOMING";
                }

            } catch (NumberFormatException e) {
                System.err.println("Fehler beim Parsen von Remote-Port: " + remotePort);
            }
        }

        return "UNKNOWN";
    }

    @Override
    public String toString() {
        return "Connection [Local=" + localAddress + ":" + localPort +
                ", Remote=" + remoteAddress + ":" + remotePort + ", State=" + state + "]";
    }
}