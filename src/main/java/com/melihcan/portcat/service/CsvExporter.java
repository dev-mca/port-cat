package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Schreibt Verbindungen als CSV. */
//Mithilfe von KI generiert
public final class CsvExporter {

    private static final String HEADER = "LocalAddress,LocalPort,RemoteAddress,RemotePort,State,Direction";

    private CsvExporter() {
    }

    /** Schreibt die Verbindungen als UTF-8-CSV in die angegebene Datei. */
    public static void export(Path file, List<Connection> data) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeCsv(writer, data);
        }
    }

    public static void writeCsv(Writer writer, List<Connection> data) throws IOException {
        writer.write(HEADER);
        writer.write(System.lineSeparator());

        for (Connection c : data) {
            writer.write(csvEscape(c.getLocalAddress()));
            writer.write(',');
            writer.write(Integer.toString(c.getLocalPort()));
            writer.write(',');
            writer.write(csvEscape(c.getRemoteAddress()));
            writer.write(',');
            writer.write(csvEscape(c.getRemotePort()));
            writer.write(',');
            writer.write(csvEscape(c.getState()));
            writer.write(',');
            writer.write(csvEscape(c.getDirection()));
            writer.write(System.lineSeparator());
        }
        writer.flush();
    }

    static String csvEscape(String field) {
        if (field == null) {
            return "";
        }
        boolean mustQuote = field.contains(",")
                || field.contains("\"")
                || field.contains("\n")
                || field.contains("\r");
        if (!mustQuote) {
            return field;
        }
        return '"' + field.replace("\"", "\"\"") + '"';
    }
}
