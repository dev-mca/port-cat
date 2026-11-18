package com.melihcan.portcat.service.parser;

import com.melihcan.portcat.model.Connection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowsParserTest {

    @Test
    void parseReadsArrayAndSkipsWildcards() {
        String json = "["
                + "{\"LocalAddress\":\"192.168.1.20\",\"LocalPort\":51234,"
                + "\"RemoteAddress\":\"140.82.121.4\",\"RemotePort\":443,\"State\":5},"
                + "{\"LocalAddress\":\"0.0.0.0\",\"LocalPort\":135,"
                + "\"RemoteAddress\":\"0.0.0.0\",\"RemotePort\":0,\"State\":2}"
                + "]";

        List<Connection> result = WindowsParser.parse(json);

        assertEquals(1, result.size());
        Connection c = result.get(0);
        assertEquals("192.168.1.20", c.getLocalAddress());
        assertEquals(51234, c.getLocalPort());
        assertEquals("140.82.121.4", c.getRemoteAddress());
        assertEquals("443", c.getRemotePort());
        assertEquals("Established", c.getState());
    }

    @Test
    void parseReadsSingleObjectAndLabelsLoopback() {
        String json = "{\"LocalAddress\":\"127.0.0.1\",\"LocalPort\":5432,"
                + "\"RemoteAddress\":\"0.0.0.0\",\"RemotePort\":0,\"State\":2}";

        List<Connection> result = WindowsParser.parse(json);

        assertEquals(1, result.size());
        assertEquals("127.0.0.1 (Loopback)", result.get(0).getLocalAddress());
        assertEquals("Listen", result.get(0).getState());
    }

    @Test
    void parseReturnsEmptyListForEmptyOutput() {
        assertTrue(WindowsParser.parse("").isEmpty());
        assertTrue(WindowsParser.parse(null).isEmpty());
    }

    @Test
    void mapStateKeepsUnknownValues() {
        assertEquals("Time-Wait", WindowsParser.mapState("11"));
        assertEquals("42", WindowsParser.mapState("42"));
    }
}
