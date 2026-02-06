package com.melihcan.portcat.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DnsResolver {

    public static String resolveDns(String ipAddress) {
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