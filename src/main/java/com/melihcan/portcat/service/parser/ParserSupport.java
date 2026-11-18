package com.melihcan.portcat.service.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Gemeinsame Hilfsfunktionen der Parser. */
final class ParserSupport {

    private ParserSupport() {
    }

    /** Liest die komplette Ausgabe eines Prozesses (Zeilen mit '\n' getrennt). */
    static String readAll(Process p) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    /** Wildcard-Adressen (alle Interfaces) werden nicht angezeigt. */
    static boolean isWildcard(String addr) {
        return "::".equals(addr) || "::1".equals(addr) || "0.0.0.0".equals(addr);
    }

    static String labelLoopback(String addr) {
        return "127.0.0.1".equals(addr) ? addr + " (Loopback)" : addr;
    }
}
