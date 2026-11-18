package com.melihcan.portcat.service.parser;

import com.melihcan.portcat.model.Connection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinuxParserTest {

    @Test
    void parseTextReadsSsOutput() {
        String output = """
                LISTEN    0      4096               127.0.0.1:631                 0.0.0.0:*
                ESTAB     0      0              192.168.1.20:51234           140.82.121.4:443
                """;

        List<Connection> result = LinuxParser.parseText(output);

        assertEquals(2, result.size());

        Connection listen = result.get(0);
        assertEquals("127.0.0.1 (Loopback)", listen.getLocalAddress());
        assertEquals(631, listen.getLocalPort());
        assertEquals("Listen", listen.getState());

        Connection established = result.get(1);
        assertEquals("192.168.1.20", established.getLocalAddress());
        assertEquals(51234, established.getLocalPort());
        assertEquals("140.82.121.4", established.getRemoteAddress());
        assertEquals("443", established.getRemotePort());
        assertEquals("Established", established.getState());
    }

    @Test
    void parseTextSkipsWildcardAddressesAndGarbage() {
        String output = """
                LISTEN    0      128                  0.0.0.0:22                  0.0.0.0:*
                this is not a socket line
                """;

        assertTrue(LinuxParser.parseText(output).isEmpty());
    }

    @Test
    void parseTextKeepsStatesWithHyphen() {
        String output = """
                TIME-WAIT 0      0              192.168.1.20:51240           140.82.121.4:443
                CLOSE-WAIT 1     0              192.168.1.20:51241           140.82.121.5:443
                """;

        List<Connection> result = LinuxParser.parseText(output);

        assertEquals(2, result.size());
        assertEquals("Time-Wait", result.get(0).getState());
        assertEquals("Close-Wait", result.get(1).getState());
    }

    @Test
    void parseJsonReadsSocketsArray() {
        String json = """
                {"sockets":[
                  {"state":"ESTAB","local_addr":"10.0.0.5","local_port":40000,
                   "remote_addr":"10.0.0.1","remote_port":"5432"},
                  {"state":"LISTEN","local_addr":"0.0.0.0","local_port":22,
                   "remote_addr":"0.0.0.0","remote_port":"*"}
                ]}
                """;

        List<Connection> result = LinuxParser.parseJson(json);

        assertEquals(1, result.size());
        assertEquals("10.0.0.5", result.get(0).getLocalAddress());
        assertEquals(40000, result.get(0).getLocalPort());
        assertEquals("5432", result.get(0).getRemotePort());
        assertEquals("Established", result.get(0).getState());
    }

    @Test
    void parseJsonReturnsEmptyListForNonJsonOutput() {
        assertTrue(LinuxParser.parseJson("ss: invalid option -- 'J'").isEmpty());
        assertTrue(LinuxParser.parseJson("").isEmpty());
    }
}
