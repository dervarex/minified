package com.dervarex.minified.modrinth.projects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SideSupportTest {

    @Test
    void fromApiValue_returnsEnum_whenMatch() {
        assertEquals(SideSupport.REQUIRED, SideSupport.fromApiValue("required"));
        assertEquals(SideSupport.OPTIONAL, SideSupport.fromApiValue("optional"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(SideSupport.REQUIRED, SideSupport.fromApiValue("REQUIRED"));
    }

    @Test
    void fromApiValue_returnsUnknown_whenNoMatch() {
        assertEquals(SideSupport.UNKNOWN, SideSupport.fromApiValue("not_real"));
    }

    @Test
    void fromApiValue_returnsNull_whenNull() {
        assertNull(SideSupport.fromApiValue(null));
    }

    @Test
    void getApiValue_returnsValue() {
        assertEquals("required", SideSupport.REQUIRED.getApiValue());
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("optional", SideSupport.OPTIONAL.toString());
    }
}