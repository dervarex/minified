package com.dervarex.minified.modrinth.versions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyTypeTest {

    @Test
    void fromApiValue_returnsEnum_whenMatch() {
        assertEquals(DependencyType.REQUIRED, DependencyType.fromApiValue("required"));
        assertEquals(DependencyType.OPTIONAL, DependencyType.fromApiValue("optional"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(DependencyType.REQUIRED, DependencyType.fromApiValue("REQUIRED"));
    }

    @Test
    void fromApiValue_returnsUnknown_whenNoMatch() {
        assertEquals(DependencyType.UNKNOWN, DependencyType.fromApiValue("nope"));
    }

    @Test
    void fromApiValue_returnsNull_whenNull() {
        assertNull(DependencyType.fromApiValue(null));
    }

    @Test
    void getApiValue_returnsValue() {
        assertEquals("required", DependencyType.REQUIRED.getApiValue());
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("optional", DependencyType.OPTIONAL.toString());
    }
}