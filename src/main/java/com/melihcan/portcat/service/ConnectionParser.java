package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionParser {

    /* -------- Betriebssystem erkennen -------- */
    private static final boolean IS_WINDOWS =
            System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");

    /* -------- Kommandos -------- */
    private static final String[] POWERSHELL_CMD = {"powershell.exe", "-NoProfile", "-Command",
            "@(Get-NetTCPConnection) | Select-Object LocalAddress,LocalPort,RemoteAddress,RemotePort,State | ConvertTo-Json -Compress"};

    private static final String SS_JSON = "ss -tnH -J";
    private static final String SS_TEXT = "ss -tanH";

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
        Process proc = null;
        try {
            long startTime = System.currentTimeMillis();

            proc = new ProcessBuilder(POWERSHELL_CMD)
                    .redirectErrorStream(true)
                    .start();

            String json = readAll(proc);

            boolean finished = proc.waitFor(10, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("PowerShell-Aufruf dauerte: " + duration + "ms");

            if (!finished) {
                System.err.println("TIMEOUT: PowerShell brauchte länger als 10 Sekunden!");
                proc.destroyForcibly();
                return list;
            }

            int exitCode = proc.exitValue();
            if (exitCode != 0) {
                System.err.println("PowerShell Exit-Code: " + exitCode);
                System.err.println("Output: " + json);
                return list;
            }

            if (json.length() < 2) {
                System.err.println("PowerShell lieferte keine Daten: '" + json + "'");
                return list;
            }

            json = json.trim();

            System.out.println("JSON erste 100 Zeichen: " + json.substring(0, Math.min(100, json.length())));

            if (json.startsWith("[")) {
                json = json.substring(1, json.length() - 1);
            } else if (json.startsWith("{") && !json.contains("},{")) {
                Connection c = parseWinObject(json.replace("{", "").replace("}", ""));
                if (c != null) list.add(c);
                System.out.println("Anzahl Verbindungen gefunden: 1");
                return list;
            } else if (!json.startsWith("{")) {
                System.err.println("Unerwartetes JSON-Format: " + json.substring(0, Math.min(100, json.length())));
                return list;
            }

            String[] objects = json.split("\\},\\{");
            System.out.println("Anzahl Verbindungen gefunden: " + objects.length);

            for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "");
                Connection c = parseWinObject(obj);
                if (c != null) list.add(c);
            }

        } catch (IOException e) {
            System.err.println("IOException beim PowerShell-Aufruf: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("InterruptedException beim PowerShell-Aufruf");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            if (proc != null && proc.isAlive()) {
                proc.destroyForcibly();
            }
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
            String stateRaw = value(raw, "State");

            // WICHTIG: State von Nummer zu Text konvertieren!
            String state = mapState(stateRaw);

            return new Connection(localAddr, localPort, remoteAddr, remotePort, state);
        } catch (Exception ex) {
            System.err.println("Fehler beim Parsen von: " + raw);
            ex.printStackTrace();
            return null;
        }
    }

    /* =====================================================================
       Linux
    ===================================================================== */
    private List<Connection> parseLinux() {
        List<Connection> list;

        list = trySsJson();
        if (!list.isEmpty()) return list;

        return trySsText();
    }

    private List<Connection> trySsJson() {
        List<Connection> list = new ArrayList<>();
        try {
            Process proc = new ProcessBuilder("bash", "-c", SS_JSON)
                    .redirectErrorStream(true).start();

            String json = readAll(proc).trim();
            if (json.isEmpty() || json.charAt(0) != '{') return list;

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
                state = stateFromName(state);

                list.add(new Connection(localAddr, localPort, remoteAddr, remotePort, state));
            }
        } catch (IOException ignored) { }
        return list;
    }

    private List<Connection> trySsText() {
        List<Connection> list = new ArrayList<>();
        try {
            Process proc = new ProcessBuilder("bash", "-c", SS_TEXT)
                    .redirectErrorStream(true).start();

            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            Pattern p = Pattern.compile(
                    "(\\w+)\\s+\\d+\\s+\\d+\\s+([^:]+):(\\d+)\\s+([^:]+):(\\S+)");
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line.trim());
                if (!m.matches()) continue;

                String state      = stateFromName(m.group(1));
                String localAddr  = m.group(2);
                if (skip(localAddr)) continue;
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

    /**
     * WICHTIG: PowerShell gibt State als Nummer zurück!
     * Quelle: https://docs.microsoft.com/en-us/dotnet/api/system.net.networkinformation.tcpstate
     */
    private static String mapState(String numeric) {
        return switch (numeric) {
            case "1" -> "Closed";
            case "2" -> "Listen";           // ← INCOMING!
            case "3" -> "SYN-Sent";
            case "4" -> "SYN-Recv";
            case "5" -> "Established";      // ← OUTGOING (meistens)
            case "6" -> "Fin-Wait-1";
            case "7" -> "Fin-Wait-2";
            case "8" -> "Close-Wait";
            case "9" -> "Closing";
            case "10" -> "Last-Ack";
            case "11" -> "Time-Wait";
            case "12" -> "Delete-TCB";
            default -> numeric;  // Falls es doch schon Text ist
        };
    }

    private static String stateFromName(String name) {
        return switch (name) {
            case "ESTAB" -> "Established";
            case "LISTEN" -> "Listen";
            case "CLOSE-WAIT" -> "Close-Wait";
            case "TIME-WAIT" -> "Time-Wait";
            default -> name;
        };
    }
}