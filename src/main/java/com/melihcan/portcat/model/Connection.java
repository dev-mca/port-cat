package com.melihcan.portcat.model;

import com.melihcan.portcat.service.PortResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Connection {
    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private String remotePort;
    private String state;

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
     * - Eingehend (INCOMING) wenn State = "Listen" (localhost horcht)
     * - Ausgehend (OUTGOING) wenn State = "Established"
     * oder wenn Remote-Port standardmäßig ist (HTTP, HTTPS, etc.)
     */
    public String getDirection() {
        if (state == null) return "UNKNOWN";

        String stateUpper = state.toUpperCase();

        // Port-Nummer auslesen
        try {
            String portStr = remotePort.split(" ")[0]; // Falls "443 (HTTPS)" -> "443"
            int port = Integer.parseInt(portStr);

            // Häufige Server-Ports -> eingehend
            if (port == 80 || port == 443 || port == 22 || port == 21 || port == 25 || port == 3306 || port == 5432) {
                if (stateUpper.contains("LISTEN")) {
                    return "INCOMING"; // Server wartet auf Verbindung
                }
            }
        } catch (Exception e) {
            // Fehler beim Parsen -> Standard-Logik
        }

        if (stateUpper.contains("LISTEN")) {
            return "INCOMING";
        } else if (stateUpper.contains("ESTABLISHED")) {
            return "OUTGOING";
        }

        return "UNKNOWN";
    }

    @Override
    public String toString() {
        return "Connection [Local=" + localAddress + ":" + localPort +
                ", Remote=" + remoteAddress + ":" + remotePort + ", State=" + state + "]";
    }
}