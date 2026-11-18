package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;

import java.util.List;

/**
 * Liefert die aktuellen TCP-Verbindungen und reichert sie für die Anzeige an:
 * Remote-Port bekommt den Dienstnamen, Remote-Adresse optional den Hostnamen.
 */
public class ConnectionService {

    private final ConnectionParser parser;
    private volatile boolean dnsEnabled = false;

    public ConnectionService() {
        this(new ConnectionParser());
    }

    public ConnectionService(ConnectionParser parser) {
        this.parser = parser;
    }

    public void setDnsEnabled(boolean enabled) {
        this.dnsEnabled = enabled;
        System.out.println("DNS-Auflösung " + (enabled ? "AKTIVIERT" : "DEAKTIVIERT"));
    }

    public boolean isDnsEnabled() {
        return dnsEnabled;
    }

    public List<Connection> getConnections() {
        boolean resolveDns = dnsEnabled;
        return parser.getConnections().stream()
                .map(c -> enrich(c, resolveDns))
                .toList();
    }

    static Connection enrich(Connection raw, boolean resolveDns) {
        String remoteAddress = raw.getRemoteAddress();
        if (resolveDns) {
            String hostname = DnsResolver.resolveDns(remoteAddress);
            if (hostname != null && !hostname.equals(remoteAddress)) {
                remoteAddress = remoteAddress + " (" + hostname + ")";
            }
        }
        return new Connection(
                raw.getLocalAddress(),
                raw.getLocalPort(),
                remoteAddress,
                PortResolver.resolvePort(raw.getRemotePort()),
                raw.getState());
    }
}
