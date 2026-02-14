package com.melihcan.portcat.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

    /* -------- Gson für JSON-Parsing -------- */
    private static final Gson gson = new Gson();

    /* -------- DNS-Auflösung aktiviert? -------- */
    private boolean dnsEnabled = false;

    public void setDnsEnabled(boolean enabled) {
        this.dnsEnabled = enabled;
        System.out.println("DNS-Auflösung " + (enabled ? "AKTIVIERT" : "DEAKTIVIERT"));
    }

    public boolean isDnsEnabled() {
        return dnsEnabled;
    }

    /* =====================================================================
       Öffentliche API
    ===================================================================== */
    public List<Connection> getConnections() {
        return IS_WINDOWS ? parseWindows() : parseLinux();
    }

    /* =====================================================================
       Windows - MIT GSON
    ===================================================================== */
    //Mithilfe von KI geschrieben
    private List<Connection> parseWindows() {
        List<Connection> list = new ArrayList<>();
        Process proc = null;
        try {
            long startTime = System.currentTimeMillis();

            proc = new ProcessBuilder(POWERSHELL_CMD)
                    .redirectErrorStream(true)
                    .start();

            String json = readAll(proc);

            boolean finished = proc.waitFor(5, TimeUnit.SECONDS);  // Reduziert von 10 auf 5s

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("PowerShell-Aufruf dauerte: " + duration + "ms");

            if (!finished) {
                System.err.println("TIMEOUT: PowerShell brauchte länger als 5 Sekunden!");
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

            // GSON-Parsing
            try {
                if (json.startsWith("[")) {
                    // Array von Objekten
                    JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
                    System.out.println("Anzahl Verbindungen gefunden: " + jsonArray.size());

                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject obj = jsonArray.get(i).getAsJsonObject();
                        Connection c = parseWinObjectGson(obj);
                        if (c != null) list.add(c);
                    }

                } else if (json.startsWith("{")) {
                    // Einzelnes Objekt
                    JsonObject obj = gson.fromJson(json, JsonObject.class);
                    Connection c = parseWinObjectGson(obj);
                    if (c != null) list.add(c);
                    System.out.println("Anzahl Verbindungen gefunden: 1");
                }
            } catch (Exception e) {
                System.err.println("GSON-Parsing Fehler: " + e.getMessage());
                e.printStackTrace();
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

    /**
     * Parst ein Windows Connection-Object mit GSON
     */
    private Connection parseWinObjectGson(JsonObject obj) {
        try {
            String localAddr = obj.get("LocalAddress").getAsString();
            if (skip(localAddr)) return null;
            if ("127.0.0.1".equals(localAddr)) localAddr += " (Loopback)";

            int localPort = obj.get("LocalPort").getAsInt();
            String remoteAddr = obj.get("RemoteAddress").getAsString();
            String remotePort = obj.get("RemotePort").getAsString();
            String stateRaw = obj.get("State").getAsString();

            // State von Nummer zu Text konvertieren
            String state = mapState(stateRaw);

            // Connection mit DNS-Toggle erstellen
            return new Connection(localAddr, localPort, remoteAddr, remotePort, state, dnsEnabled);

        } catch (Exception ex) {
            System.err.println("Fehler beim Parsen von JSON-Object: " + obj);
            ex.printStackTrace();
            return null;
        }
    }

    /* =====================================================================
       Linux - MIT GSON
    ===================================================================== */
    private List<Connection> parseLinux() {
        List<Connection> list;

        list = trySsJson();
        if (!list.isEmpty()) return list;

        return trySsText();
    }

    /**
     * Linux ss JSON-Modus mit GSON
     */
    private List<Connection> trySsJson() {
        List<Connection> list = new ArrayList<>();
        try {
            Process proc = new ProcessBuilder("bash", "-c", SS_JSON)
                    .redirectErrorStream(true).start();

            String json = readAll(proc).trim();
            if (json.isEmpty() || json.charAt(0) != '{') return list;

            try {
                // Parse mit GSON
                JsonObject root = gson.fromJson(json, JsonObject.class);

                // Finde das Array (meist unter key wie "sockets" oder direkt)
                JsonArray socketsArray = null;
                for (String key : root.keySet()) {
                    if (root.get(key).isJsonArray()) {
                        socketsArray = root.get(key).getAsJsonArray();
                        break;
                    }
                }

                if (socketsArray == null) return list;

                for (int i = 0; i < socketsArray.size(); i++) {
                    JsonObject socket = socketsArray.get(i).getAsJsonObject();

                    String state = socket.has("state") ? socket.get("state").getAsString() : "";
                    String localAddr = socket.has("local_addr") ? socket.get("local_addr").getAsString() : "";

                    if (skip(localAddr)) continue;
                    if ("127.0.0.1".equals(localAddr)) localAddr += " (Loopback)";

                    int localPort = socket.has("local_port") ? socket.get("local_port").getAsInt() : 0;
                    String remoteAddr = socket.has("remote_addr") ? socket.get("remote_addr").getAsString() : "";
                    String remotePort = socket.has("remote_port") ? socket.get("remote_port").getAsString() : "";

                    state = stateFromName(state);

                    list.add(new Connection(localAddr, localPort, remoteAddr, remotePort, state, dnsEnabled));
                }
            } catch (Exception e) {
                System.err.println("GSON Linux JSON Parsing Fehler: " + e.getMessage());
            }

        } catch (IOException ignored) { }
        return list;
    }

    /**
     * Linux ss Text-Modus (Fallback)
     */
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

                list.add(new Connection(localAddr, localPort, remoteAddr, remotePort, state, dnsEnabled));
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

    private static boolean skip(String addr) {
        return "::".equals(addr) || "::1".equals(addr) || "0.0.0.0".equals(addr);
    }

    /**
     * PowerShell gibt State als Nummer zurück!
     * Quelle: https://docs.microsoft.com/en-us/dotnet/api/system.net.networkinformation.tcpstate
     */
    private static String mapState(String numeric) {
        return switch (numeric) {
            case "1" -> "Closed";
            case "2" -> "Listen";
            case "3" -> "SYN-Sent";
            case "4" -> "SYN-Recv";
            case "5" -> "Established";
            case "6" -> "Fin-Wait-1";
            case "7" -> "Fin-Wait-2";
            case "8" -> "Close-Wait";
            case "9" -> "Closing";
            case "10" -> "Last-Ack";
            case "11" -> "Time-Wait";
            case "12" -> "Delete-TCB";
            default -> numeric;
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