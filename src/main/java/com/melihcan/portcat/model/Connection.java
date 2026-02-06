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

    @Override
    public String toString() {
        return "Connection [Local=" + localAddress + ":" + localPort +
                ", Remote=" + remoteAddress + ":" + remotePort + ", State=" + state + "]";
    }
}