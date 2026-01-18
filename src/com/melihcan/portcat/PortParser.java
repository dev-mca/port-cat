package com.melihcan.portcat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PortParser {

    static final String CMD = "powershell.exe -Command \"Get-NetTCPConnection | Select-Object LocalAddress, LocalPort, RemoteAddress, RemotePort, State | ConvertTo-Json -Compress\"";

    public List<Connection> getConnections() {
        List<Connection> connectionList = new ArrayList<>();

        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", CMD);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder jsonOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }

            String json = jsonOutput.toString();

            if (json.length() > 2) {
                json = json.substring(1, json.length() - 1);


                // Split JSON-Format
                String[] objects = json.split("\\},\\{");

                for (String obj : objects) {
                    obj = obj.replace("{", "").replace("}", "");
                    Connection conn = parseSingleObject(obj);
                    if (conn != null) {
                        connectionList.add(conn);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }

        return connectionList;
    }

    //Erstellt ein Connection-Objekt, um getConnections() zu vereinfachen
    private Connection parseSingleObject(String rawData) {
        try {

            String localAddr = extractValue(rawData, "LocalAddress");

            if (localAddr.equals("::") || localAddr.equals("::1") || localAddr.equals("0.0.0.0")) {
                return null; // Ignorieren, gibt null zurück
            }
            if (localAddr.equals("127.0.0.1")) {
                localAddr = "127.0.0.1 (Loopback)";
            }

            String remoteAddr = extractValue(rawData, "RemoteAddress");

            String state = extractValue(rawData, "State");

            switch (state) {
                case "5":
                    state = "Established";
                    break;
                case "2":
                    state = "Listening";
                    break;
                case "1":
                    state = "Closed";
                    break;
                case "3":
                    state = "Establishing outgoing";
                    break;
                case "4":
                    state = "Establishing ingoing";
                    break;
                case "8":
                    state = "Remote disconnected --> cleaning up";
                    break;
                case "9":
                    state = "Remote disconnected --> remote awaits ack from you";
                case "11":
                    state = "Remote disconnected --> safe wait for packages";
                    break;
            }

            // Ports sind Zahlen, daher Integer.parseInt
            int localPort = Integer.parseInt(extractValue(rawData, "LocalPort"));

            String remotePort = extractValue(rawData, "RemotePort");

            switch (remotePort) {
                case "443":
                    remotePort = "443 (HTTPS)";
                    break;
                case "80":
                    remotePort = "80 (HTTP)";
                    break;
                case "22":
                    remotePort = "22 (SSH)";
                    break;
                default:
                    break;
            }


            return new Connection(localAddr, localPort, remoteAddr, remotePort, state);
        } catch (Exception e) {
            // Falls mal ein Feld fehlt oder leer ist, ignorieren wir es
            return null;
        }
    }


    // Hilfsmethode: Sucht den Wert zu einem Key (z.B. "LocalAddress")
    private String extractValue(String data, String key) {
        // Wir suchen nach: "Key": "Wert" oder "Key": 123
        String searchKey = "\"" + key + "\":";
        int start = data.indexOf(searchKey);
        if (start == -1) return "0"; // Fallback falls nicht gefunden

        start += searchKey.length();

        // Finden, wo der Wert endet (beim nächsten Komma oder am Ende)
        int end = data.indexOf(",", start);
        if (end == -1) end = data.length();

        String value = data.substring(start, end).trim();

        // Anführungszeichen entfernen, falls es ein String ist
        return value.replace("\"", "");
    }
}
