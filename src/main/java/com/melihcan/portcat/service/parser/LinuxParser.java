package com.melihcan.portcat.service.parser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melihcan.portcat.model.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Liest Verbindungen unter Linux über {@code ss} (JSON-Modus, Fallback Text-Modus). */
public class LinuxParser implements ConnectionParser {

    private static final String SS_JSON = "ss -tnH -J";
    private static final String SS_TEXT = "ss -tanH";

    private static final Pattern SS_TEXT_LINE = Pattern.compile(
            "(\\S+)\\s+\\d+\\s+\\d+\\s+([^:]+):(\\d+)\\s+([^:]+):(\\S+)");

    private static final Gson GSON = new Gson();

    @Override
    public List<Connection> getConnections() {
        List<Connection> list = parseJson(run(SS_JSON));
        if (!list.isEmpty()) {
            return list;
        }
        return parseText(run(SS_TEXT));
    }

    private static String run(String command) {
        try {
            Process proc = new ProcessBuilder("bash", "-c", command)
                    .redirectErrorStream(true)
                    .start();
            return ParserSupport.readAll(proc);
        } catch (IOException e) {
            System.err.println("Fehler beim Ausführen von '" + command + "': " + e.getMessage());
            return "";
        }
    }

    /** Parst die Ausgabe von {@code ss -J}. Liefert eine leere Liste, wenn es kein JSON ist. */
    static List<Connection> parseJson(String output) {
        List<Connection> list = new ArrayList<>();
        String json = output == null ? "" : output.trim();
        if (json.isEmpty() || json.charAt(0) != '{') {
            return list;
        }

        try {
            JsonObject root = GSON.fromJson(json, JsonObject.class);

            // Finde das Array (meist unter key wie "sockets" oder direkt)
            JsonArray socketsArray = null;
            for (String key : root.keySet()) {
                if (root.get(key).isJsonArray()) {
                    socketsArray = root.get(key).getAsJsonArray();
                    break;
                }
            }
            if (socketsArray == null) {
                return list;
            }

            for (JsonElement element : socketsArray) {
                JsonObject socket = element.getAsJsonObject();

                String localAddr = getString(socket, "local_addr");
                if (ParserSupport.isWildcard(localAddr)) {
                    continue;
                }

                int localPort = socket.has("local_port") ? socket.get("local_port").getAsInt() : 0;
                String remoteAddr = getString(socket, "remote_addr");
                String remotePort = getString(socket, "remote_port");
                String state = stateFromName(getString(socket, "state"));

                list.add(new Connection(ParserSupport.labelLoopback(localAddr), localPort,
                        remoteAddr, remotePort, state));
            }
        } catch (Exception e) {
            System.err.println("GSON Linux JSON Parsing Fehler: " + e.getMessage());
        }
        return list;
    }

    /** Parst die Text-Ausgabe von {@code ss -tanH}. */
    static List<Connection> parseText(String output) {
        List<Connection> list = new ArrayList<>();
        if (output == null) {
            return list;
        }

        for (String line : output.split("\n")) {
            Matcher m = SS_TEXT_LINE.matcher(line.trim());
            if (!m.matches()) {
                continue;
            }

            String localAddr = m.group(2);
            if (ParserSupport.isWildcard(localAddr)) {
                continue;
            }

            String state = stateFromName(m.group(1));
            int localPort = Integer.parseInt(m.group(3));
            String remoteAddr = m.group(4);
            String remotePort = m.group(5);

            list.add(new Connection(ParserSupport.labelLoopback(localAddr), localPort,
                    remoteAddr, remotePort, state));
        }
        return list;
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : "";
    }

    static String stateFromName(String name) {
        return switch (name) {
            case "ESTAB" -> "Established";
            case "LISTEN" -> "Listen";
            case "CLOSE-WAIT" -> "Close-Wait";
            case "TIME-WAIT" -> "Time-Wait";
            default -> name;
        };
    }
}
