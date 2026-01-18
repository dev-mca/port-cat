package com.melihcan.portcat;

public class Connection {
    // Einfache Variablen für unsere Daten
    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private String remotePort;
    private String state;

    // Der Konstruktor ("Baumeister")
    public Connection(String localAddress, int localPort, String remoteAddress, String remotePort, String state) {
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.state = state;
    }

    // Getter-Methoden (damit die GUI die Daten lesen kann)
    public String getLocalAddress() { return localAddress; }
    public int getLocalPort() { return localPort; }
    public String getRemoteAddress() { return remoteAddress; }
    public String getRemotePort() { return remotePort; }
    public String getState() { return state; }

    // Optional: Für schöneren Text in der Konsole
    @Override
    public String toString() {
        return "Connection [Local=" + localAddress + ":" + localPort +
                ", Remote=" + remoteAddress + ":" + remotePort + ", State=" + state + "]";
    }
}
