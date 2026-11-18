package com.melihcan.portcat.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class DnsResolver {

    private DnsResolver() {
    }

    /**
     * Löst eine IP-Adresse in einen Hostnamen auf.
     * ACHTUNG: Kann 0.5-3 Sekunden dauern!
     *
     * @return Hostname, oder die IP selbst wenn nicht auflösbar
     */
    public static String resolveDns(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return ipAddress;
        }
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            String hostname = address.getHostName();

            // Falls gleich wie IP, dann nicht auflösbar
            if (hostname.equals(ipAddress)) {
                return ipAddress;
            }
            return hostname;
        } catch (UnknownHostException e) {
            return ipAddress; // Falls Fehler, IP zurückgeben
        }
    }
}
