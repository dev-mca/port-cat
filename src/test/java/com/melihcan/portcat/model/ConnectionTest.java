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
        assertEquals("10.0.0.1", conn.getRemoteAddress());
        assertEquals("443", conn.getRemotePort());
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
}