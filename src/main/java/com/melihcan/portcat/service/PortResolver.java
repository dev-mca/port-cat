package com.melihcan.portcat.service;

import java.util.HashMap;
import java.util.Map;


//Mithilfe von KI generiert
public class PortResolver {

    private static final Map<Integer, String> PORT_NAMES = new HashMap<>();

    static {
        // Core / sehr häufig
        PORT_NAMES.put(20, "FTP Data");
        PORT_NAMES.put(21, "FTP");
        PORT_NAMES.put(22, "SSH");
        PORT_NAMES.put(23, "Telnet");
        PORT_NAMES.put(25, "SMTP");
        PORT_NAMES.put(53, "DNS");
        PORT_NAMES.put(80, "HTTP");
        PORT_NAMES.put(110, "POP3");
        PORT_NAMES.put(123, "NTP");
        PORT_NAMES.put(143, "IMAP");
        PORT_NAMES.put(443, "HTTPS");

        // Weitere häufige Dienste
        PORT_NAMES.put(1433, "MSSQL");
        PORT_NAMES.put(3306, "MySQL");
        PORT_NAMES.put(3389, "RDP");
        PORT_NAMES.put(5432, "PostgreSQL");
        PORT_NAMES.put(5900, "VNC");
        PORT_NAMES.put(6379, "Redis");
        PORT_NAMES.put(8080, "HTTP Alt");
        PORT_NAMES.put(8443, "HTTPS Alt");
        PORT_NAMES.put(27017, "MongoDB");
    }

    public static String resolvePort(String portString) {
        final int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            return portString;
        }

        String name = PORT_NAMES.get(port);
        if (name != null) {
            return port + " (" + name + ")";
        }

        String range;
        if (port >= 0 && port <= 1023) range = "Well-known";
        else if (port <= 49151) range = "Registered";
        else if (port <= 65535) range = "Dynamic/Private";
        else range = "Unbekannt";

        return port + " (" + range + ")";
    }
}