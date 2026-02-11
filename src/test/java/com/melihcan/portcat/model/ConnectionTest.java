package com.melihcan.portcat.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {

    @Test
    void testConnectionCreation() {
        // Arrange
        Connection conn = new Connection("192.168.1.1", 8080, "10.0.0.1", "443", "ESTABLISHED");

        // Act & Assert
        assertEquals("192.168.1.1", conn.getLocalAddress());
        assertEquals(8080, conn.getLocalPort());
        assertTrue(conn.getRemoteAddress().contains("10.0.0.1"));
        assertEquals("443 (HTTPS)", conn.getRemotePort());
        assertEquals("ESTABLISHED", conn.getState());
    }

    @Test
    void testConnectionToString() {
        Connection conn = new Connection("127.0.0.1", 3306, "192.168.1.100", "50000", "LISTEN");
        String result = conn.toString();

        assertTrue(result.contains("127.0.0.1"));
        assertTrue(result.contains("3306"));
        assertTrue(result.contains("LISTEN"));
    }

    @Test
    void testConnectionWithNullValues() {
        Connection conn = new Connection(null, 0, null, null, null);

        assertNull(conn.getLocalAddress());
        assertEquals(0, conn.getLocalPort());
        assertNull(conn.getState());
    }

    // ===== NEU: Direction Tests =====

    @Test
    void testDirectionIncoming() {
        // LISTEN State -> Eingehend
        Connection conn = new Connection("192.168.1.1", 8080, "10.0.0.1", "443", "LISTEN");
        assertEquals("INCOMING", conn.getDirection());
    }

    @Test
    void testDirectionOutgoing() {
        // ESTABLISHED State -> Ausgehend
        Connection conn = new Connection("192.168.1.1", 8080, "10.0.0.1", "443", "ESTABLISHED");
        assertEquals("OUTGOING", conn.getDirection());
    }

    @Test
    void testDirectionUnknown() {
        // State = null -> Unknown
        Connection conn = new Connection("192.168.1.1", 8080, "10.0.0.1", "443", null);
        assertEquals("UNKNOWN", conn.getDirection());
    }

    @Test
    void testDirectionUnknownState() {
        // Unbekannter State
        Connection conn = new Connection("192.168.1.1", 8080, "10.0.0.1", "443", "CLOSING");
        assertEquals("UNKNOWN", conn.getDirection());
    }

    @Test
    void testDirectionCaseInsensitive() {
        // State sollte case-insensitive sein
        Connection conn = new Connection("192.168.1.1", 8080, "10.0.0.1", "443", "listen");
        assertEquals("INCOMING", conn.getDirection());
    }
}