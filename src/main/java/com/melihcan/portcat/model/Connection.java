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
     * Bestimmt die Richtung der Verbindung mit DEBUG-Ausgaben
     */
    public String getDirection() {
        if (state == null) {
            System.out.println("DEBUG: State ist null → UNKNOWN");
            return "UNKNOWN";
        }

        String stateUpper = state.toUpperCase();
        System.out.println("DEBUG Direction Check: State=" + state + ", LocalPort=" + localPort + ", RemotePort=" + remotePort);

        // 1. LISTEN = immer INCOMING (wir bieten einen Dienst an)
        if (stateUpper.contains("LISTEN")) {
            System.out.println("  → INCOMING (LISTEN)");
            return "INCOMING";
        }

        // 2. ESTABLISHED = schaue welche Seite der Server ist
        if (stateUpper.contains("ESTABLISHED")) {
            try {
                // Remote-Port extrahieren (z.B. "443 (HTTPS)" → 443)
                String remotePortStr = remotePort.split(" ")[0].trim();
                System.out.println("  → Extrahierter Remote-Port String: '" + remotePortStr + "'");

                int remotePortNum = Integer.parseInt(remotePortStr);
                System.out.println("  → Remote-Port Nummer: " + remotePortNum);

                // Wenn REMOTE-Port ein bekannter Server-Port ist
                // → WIR haben uns zu einem Server verbunden → OUTGOING
                if (SERVER_PORTS.contains(remotePortNum)) {
                    System.out.println("  → OUTGOING (Remote-Port " + remotePortNum + " ist Server-Port)");
                    return "OUTGOING";
                }

                // Wenn LOCAL-Port ein bekannter Server-Port ist
                // → Jemand hat sich zu UNSEREM Server verbunden → INCOMING
                if (SERVER_PORTS.contains(localPort)) {
                    System.out.println("  → INCOMING (Local-Port " + localPort + " ist Server-Port)");
                    return "INCOMING";
                }

                // Heuristik: High Ports (>49152) sind meist Client-Ports
                // Wenn unser Port hoch ist und der Remote niedrig → OUTGOING
                if (localPort > 49152 && remotePortNum < 49152) {
                    System.out.println("  → OUTGOING (Heuristik: Local high, Remote low)");
                    return "OUTGOING";
                }

                // Wenn unser Port niedrig ist und der Remote hoch → INCOMING
                if (localPort < 49152 && remotePortNum > 49152) {
                    System.out.println("  → INCOMING (Heuristik: Local low, Remote high)");
                    return "INCOMING";
                }

                System.out.println("  → UNKNOWN (keine Regel traf zu)");

            } catch (Exception e) {
                System.err.println("  → FEHLER beim Parsen: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Andere States (CLOSE-WAIT, TIME-WAIT, etc.) oder unklare Situation
        System.out.println("  → UNKNOWN (State war nicht LISTEN oder ESTABLISHED)");
        return "UNKNOWN";
    }

    @Override
    public String toString() {
        return "Connection [Local=" + localAddress + ":" + localPort +
                ", Remote=" + remoteAddress + ":" + remotePort + ", State=" + state + "]";
    }
}