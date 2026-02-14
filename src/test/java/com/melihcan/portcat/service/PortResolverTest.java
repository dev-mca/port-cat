package com.melihcan.portcat.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PortResolverTest {

    @Test
    void testResolveKnownPort() {
        // HTTP Port
        String result = PortResolver.resolvePort("80");
        assertEquals("80 (HTTP)", result);
    }

    @Test
    void testResolveAnotherKnownPort() {
        // HTTPS Port
        String result = PortResolver.resolvePort("443");
        assertEquals("443 (HTTPS)", result);
    }

    @Test
    void testResolveSSHPort() {
        String result = PortResolver.resolvePort("22");
        assertEquals("22 (SSH)", result);
    }

    @Test
    void testResolveWellKnownPort() {
        // Port 5000 ist nicht in der Map, aber im Well-known Bereich (0-1023)
        String result = PortResolver.resolvePort("1000");
        assertTrue(result.contains("1000"));
        assertTrue(result.contains("Well-known"));
    }

    @Test
    void testResolveRegisteredPort() {
        // Port zwischen 1024 und 49151
        String result = PortResolver.resolvePort("8888");
        assertTrue(result.contains("8888"));
        assertTrue(result.contains("Registered"));
    }

    @Test
    void testResolveDynamicPort() {
        // Port zwischen 49152 und 65535
        String result = PortResolver.resolvePort("54321");
        assertTrue(result.contains("54321"));
        assertTrue(result.contains("Dynamic/Private"));
    }

    @Test
    void testResolveInvalidPort() {
        // Ungültige Port-Nummer - FIXED!
        String result = PortResolver.resolvePort("invalid");
        assertNull(result); // ✅ KORRIGIERT
    }

    @Test
    void testResolveMongoDBPort() {
        String result = PortResolver.resolvePort("27017");
        assertEquals("27017 (MongoDB)", result);
    }

    @Test
    void testResolveNegativePort() {
        // Port ist negativ -> Unbekannt
        String result = PortResolver.resolvePort("-1");
        assertTrue(result.contains("Unbekannt"));
    }

    @Test
    void testResolveAnotherNegativePort() {
        // Port ist negativ -> Unbekannt
        String result = PortResolver.resolvePort("-22");
        assertTrue(result.contains("Unbekannt"));
    }

    @Test
    void testResolveNullPort() {
        // Null-Check
        String result = PortResolver.resolvePort(null);
        assertNull(result);
    }
}