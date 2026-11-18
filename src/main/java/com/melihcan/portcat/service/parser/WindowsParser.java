package com.melihcan.portcat.service.parser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melihcan.portcat.model.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Liest Verbindungen unter Windows über PowerShell (Get-NetTCPConnection als JSON). */
//Mithilfe von KI geschrieben
public class WindowsParser implements ConnectionParser {

    private static final String[] POWERSHELL_CMD = {"powershell.exe", "-NoProfile", "-Command",
        "@(Get-NetTCPConnection) | Select-Object LocalAddress,LocalPort,RemoteAddress,RemotePort,State"
            + " | ConvertTo-Json -Compress"};

    private static final Gson GSON = new Gson();

    @Override
    public List<Connection> getConnections() {
        Process proc = null;
        try {
            long startTime = System.currentTimeMillis();

            proc = new ProcessBuilder(POWERSHELL_CMD)
                    .redirectErrorStream(true)
                    .start();

            String json = ParserSupport.readAll(proc);

            boolean finished = proc.waitFor(5, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("PowerShell-Aufruf dauerte: " + duration + "ms");

            if (!finished) {
                System.err.println("TIMEOUT: PowerShell brauchte länger als 5 Sekunden!");
                proc.destroyForcibly();
                return List.of();
            }

            int exitCode = proc.exitValue();
            if (exitCode != 0) {
                System.err.println("PowerShell Exit-Code: " + exitCode);
                System.err.println("Output: " + json);
                return List.of();
            }

            return parse(json);

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
        return List.of();
    }

    /** Parst die JSON-Ausgabe von PowerShell (ein Objekt oder ein Array von Objekten). */
    static List<Connection> parse(String json) {
        List<Connection> list = new ArrayList<>();
        json = json == null ? "" : json.trim();

        if (json.length() < 2) {
            System.err.println("PowerShell lieferte keine Daten: '" + json + "'");
            return list;
        }

        try {
            if (json.startsWith("[")) {
                JsonArray jsonArray = GSON.fromJson(json, JsonArray.class);
                System.out.println("Anzahl Verbindungen gefunden: " + jsonArray.size());
                for (JsonElement element : jsonArray) {
                    addIfValid(list, parseObject(element.getAsJsonObject()));
                }
            } else if (json.startsWith("{")) {
                addIfValid(list, parseObject(GSON.fromJson(json, JsonObject.class)));
                System.out.println("Anzahl Verbindungen gefunden: 1");
            }
        } catch (Exception e) {
            System.err.println("GSON-Parsing Fehler: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private static void addIfValid(List<Connection> list, Connection c) {
        if (c != null) {
            list.add(c);
        }
    }

    private static Connection parseObject(JsonObject obj) {
        try {
            String localAddr = obj.get("LocalAddress").getAsString();
            if (ParserSupport.isWildcard(localAddr)) {
                return null;
            }

            int localPort = obj.get("LocalPort").getAsInt();
            String remoteAddr = obj.get("RemoteAddress").getAsString();
            String remotePort = obj.get("RemotePort").getAsString();
            String state = mapState(obj.get("State").getAsString());

            return new Connection(ParserSupport.labelLoopback(localAddr), localPort, remoteAddr, remotePort, state);

        } catch (Exception ex) {
            System.err.println("Fehler beim Parsen von JSON-Object: " + obj);
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * PowerShell gibt State als Nummer zurück!
     * Quelle: https://docs.microsoft.com/en-us/dotnet/api/system.net.networkinformation.tcpstate
     */
    static String mapState(String numeric) {
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
}
