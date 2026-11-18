package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionServiceTest {

    @Test
    void enrichAddsServiceNameToRemotePort() {
        Connection raw = new Connection("192.168.1.1", 50000, "10.0.0.1", "443", "Established");

        Connection enriched = ConnectionService.enrich(raw, false);

        assertEquals("443 (HTTPS)", enriched.getRemotePort());
        assertEquals("10.0.0.1", enriched.getRemoteAddress());
        assertEquals("192.168.1.1", enriched.getLocalAddress());
        assertEquals(50000, enriched.getLocalPort());
        assertEquals("Established", enriched.getState());
    }

    @Test
    void getConnectionsEnrichesEveryParsedConnection() {
        ConnectionParser fakeParser = new ConnectionParser() {
            @Override
            public List<Connection> getConnections() {
                return List.of(
                        new Connection("10.0.0.2", 51000, "10.0.0.1", "22", "Established"),
                        new Connection("10.0.0.2", 51001, "10.0.0.1", "80", "Established"));
            }
        };

        List<Connection> result = new ConnectionService(fakeParser).getConnections();

        assertEquals("22 (SSH)", result.get(0).getRemotePort());
        assertEquals("80 (HTTP)", result.get(1).getRemotePort());
    }
}
