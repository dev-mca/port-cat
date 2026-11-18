package com.melihcan.portcat.service.parser;

import com.melihcan.portcat.model.Connection;

import java.util.List;
import java.util.Locale;

/**
 * Liest die aktuellen TCP-Verbindungen des Betriebssystems (Rohdaten, ohne Anreicherung).
 */
public interface ConnectionParser {

    List<Connection> getConnections();

    /** Wählt den passenden Parser für das laufende Betriebssystem. */
    static ConnectionParser forCurrentOs() {
        boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        return isWindows ? new WindowsParser() : new LinuxParser();
    }
}
