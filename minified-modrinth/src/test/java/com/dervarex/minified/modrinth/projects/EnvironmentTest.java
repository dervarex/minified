package com.dervarex.minified.modrinth.projects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    @Test
    void fromApiValue_returnsEnum_whenMatch() {
        assertEquals(Environment.CLIENT_AND_SERVER, Environment.fromApiValue("client_and_server"));
        assertEquals(Environment.SERVER_ONLY, Environment.fromApiValue("server_only"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(Environment.CLIENT_AND_SERVER, Environment.fromApiValue("CLIENT_AND_SERVER"));
    }

    @Test
    void fromApiValue_returnsUnknown_whenNoMatch() {
        assertEquals(Environment.UNKNOWN, Environment.fromApiValue("not_real"));
    }

    @Test
    void fromApiValue_returnsNull_whenNull() {
        assertNull(Environment.fromApiValue(null));
    }

    @Test
    void getApiValue_returnsValue() {
        assertEquals("client_and_server", Environment.CLIENT_AND_SERVER.getApiValue());
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("server_only", Environment.SERVER_ONLY.toString());
    }
}