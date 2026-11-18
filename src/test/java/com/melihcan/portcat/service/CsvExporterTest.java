package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvExporterTest {

    private static final String NL = System.lineSeparator();

    @Test
    void writesHeaderAndOneLinePerConnection() throws IOException {
        List<Connection> data = List.of(
                new Connection("192.168.1.1", 51000, "10.0.0.1", "443 (HTTPS)", "Established"),
                new Connection("127.0.0.1", 631, "0.0.0.0", "*", "Listen"));

        StringWriter out = new StringWriter();
        CsvExporter.writeCsv(out, data);

        assertEquals("LocalAddress,LocalPort,RemoteAddress,RemotePort,State,Direction" + NL
                + "192.168.1.1,51000,10.0.0.1,443 (HTTPS),Established,OUTGOING" + NL
                + "127.0.0.1,631,0.0.0.0,*,Listen,INCOMING" + NL, out.toString());
    }

    @Test
    void writesOnlyHeaderForEmptyList() throws IOException {
        StringWriter out = new StringWriter();
        CsvExporter.writeCsv(out, List.of());

        assertEquals("LocalAddress,LocalPort,RemoteAddress,RemotePort,State,Direction" + NL, out.toString());
    }

    @Test
    void escapesSpecialCharacters() {
        assertEquals("plain", CsvExporter.csvEscape("plain"));
        assertEquals("", CsvExporter.csvEscape(null));
        assertEquals("\"a,b\"", CsvExporter.csvEscape("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", CsvExporter.csvEscape("say \"hi\""));
        assertEquals("\"line\nbreak\"", CsvExporter.csvEscape("line\nbreak"));
    }
}
