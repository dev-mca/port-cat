package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionParser {

    /* -------- Betriebssystem erkennen -------- */
    private static final boolean IS_WINDOWS =
            System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");

    /* -------- Kommandos -------- */
    private static final String POWERSHELL_CMD =
            "powershell.exe -Command \"Get-NetTCPConnection | "
                    + "Select-Object LocalAddress,LocalPort,RemoteAddress,RemotePort,State "
                    + "| ConvertTo-Json -Compress\"";

    // zuerst versuchen wir die JSON-Option (-J). Scheitert das, nehmen wir Text.
    private static final String SS_JSON = "ss -tnH -J";   // -u für UDP später | JSON (requires iproute2 ≥ 5.9)
    private static final String SS_TEXT = "ss -tanH";      // Text-Fallback

    /* =====================================================================
       Öffentliche API
    ===================================================================== */
    public List<Connection> getConnections() {
        return IS_WINDOWS ? parseWindows() : parseLinux();
    }

    /* =====================================================================
       Windows
    ===================================================================== */
    private List<Connection> parseWindows() {
        List<Connection> list = new ArrayList<>();
        try {
            Process proc = new ProcessBuilder("cmd.exe", "/c", POWERSHELL_CMD)
                    .redirectErrorStream(true).start();

            String json = readAll(proc);
            // Ausgabe enthält ein JSON-Array – bei unserer Kommandokette ohne Klammern.
            if (json.length() < 2) return list;

            // Klammern einklammern -> valides Array
            json = json.trim();
            if (json.startsWith("[")) {
                json = json.substring(1, json.length() - 1);
            } else if (json.startsWith("{")) {
                // ok, single object
            } else {
                return list;
            }
            String[] objects = json.split("\\},\\{");
            for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "");
                Connection c = parseWinObject(obj);
                if (c != null) list.add(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Connection parseWinObject(String raw) {
        try {
            String localAddr  = value(raw, "LocalAddress");
            if (skip(localAddr)) return null;
            if ("127.0.0.1".equals(localAddr)) localAddr += " (Loopback)";

            int localPort  = Integer.parseInt(value(raw, "LocalPort"));
            String remoteAddr = value(raw, "RemoteAddress");
            String remotePort = value(raw, "RemotePort");
            String state      = mapState(value(raw, "State"));

            return new Connection(localAddr, localPort, remoteAddr, remotePort, state);
        } catch (Exception ex) {
            return null;
        }
    }

    /* =====================================================================
       Linux
    ===================================================================== */
    private List<Connection> parseLinux() {
        List<Connection> list;

        // 1) JSON-Versuch
        list = trySsJson();
        if (!list.isEmpty()) return list;

        // 2) Text-Fallback
        return trySsText();
    }

    /* ---------- 1) ss … -J ------------------------------------------------ */
    private List<Connection> trySsJson() {
        List<Connection> list = new ArrayList<>();
        try {
            Process proc = new ProcessBuilder("bash", "-c", SS_JSON)
                    .redirectErrorStream(true).start();

            String json = readAll(proc).trim();
            if (json.isEmpty() || json.charAt(0) != '{') return list; // kein JSON → alte Version

            /*
              JSON-Schema von ss -J ist:
              {
                 "tcp": [
                   {"state":"ESTAB",
                    "local_addr":"192.168.0.2",
                    "local_port":"22",
                    "remote_addr":"192.168.0.10",
                    "remote_port":"56544",
                    …},
                    …
                 ]
              }
             */

            // ganz leichter Parser, kein externes JSON-Lib benötigt:
            String inner = json.substring(json.indexOf('[') + 1, json.lastIndexOf(']'));
            String[] objects = inner.split("\\},\\{");
            for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "");
                String state      = value(obj, "state");
                String localAddr  = value(obj, "local_addr");
                if (skip(localAddr)) continue;
                if ("127.0.0.1".equals(localAddr)) localAddr += " (Loopback)";

                int    localPort  = Integer.parseInt(value(obj, "local_port"));
                String remoteAddr = value(obj, "remote_addr");
                String remotePort = value(obj, "remote_port");
                state = stateFromName(state); // "ESTAB", "LISTEN" …

                list.add(new Connection(localAddr, localPort, remoteAddr, remotePort, state));
            }
        } catch (IOException ignored) { }
        return list;
    }

    /* ---------- 2) ss … -tanH  (Text) ------------------------------------ */
    private List<Connection> trySsText() {
        List<Connection> list = new ArrayList<>();
        try {
            Process proc = new ProcessBuilder("bash", "-c", SS_TEXT)
                    .redirectErrorStream(true).start();

            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            /*
              Beispielausgabe (ss -tanH):
              LISTEN 0      128         0.0.0.0:22        0.0.0.0:*
              ESTAB  0      0       192.168.0.2:22    192.168.0.10:56544
              …
             */

            Pattern p = Pattern.compile(
                    "(\\w+)\\s+\\d+\\s+\\d+\\s+([^:]+):(\\d+)\\s+([^:]+):(\\S+)");
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line.trim());
                if (!m.matches()) continue;

                String state      = stateFromName(m.group(1));       // LISTEN, ESTAB …
                String localAddr  = m.group(2);
                //if (skip(localAddr)) continue;
                if ("127.0.0.1".equals(localAddr)) localAddr += " (Loopback)";
                int    localPort  = Integer.parseInt(m.group(3));
                String remoteAddr = m.group(4);
                String remotePort = m.group(5);

                list.add(new Connection(localAddr, localPort, remoteAddr, remotePort, state));
            }
        } catch (IOException ignored) { }
        return list;
    }

    /* =====================================================================
       Hilfsfunktionen
    ===================================================================== */
    private static String readAll(Process p) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    // extrahiert "Key":"Value" oder "Key":number (sehr einfacher Parser)
    private static String value(String jsonObject, String key) {
        String k = "\"" + key + "\":";
        int idx = jsonObject.indexOf(k);
        if (idx == -1) return "";
        idx += k.length();
        int end = jsonObject.indexOf(",", idx);
        if (end == -1) end = jsonObject.length();
        return jsonObject.substring(idx, end).replace("\"", "").trim();
    }

    private static boolean skip(String addr) {
        return "::".equals(addr) || "::1".equals(addr) || "0.0.0.0".equals(addr);
    }

    private static String mapState(String numeric) {
        return switch (numeric) {
            case "5" -> "Established";
            case "2" -> "Listening";
            case "1" -> "Closed";
            case "3" -> "SYN-Sent";
            case "4" -> "SYN-Recv";
            case "8" -> "Fin-Wait-1";
            case "9" -> "Fin-Wait-2";
            case "11" -> "Time-Wait";
            default -> numeric;
        };
    }

    private static String stateFromName(String name) {
        return switch (name) {
            case "ESTAB" -> "Established";
            case "LISTEN" -> "Listening";
            case "CLOSE-WAIT" -> "Close-Wait";
            case "TIME-WAIT" -> "Time-Wait";
            default -> name;
        };
    }
}