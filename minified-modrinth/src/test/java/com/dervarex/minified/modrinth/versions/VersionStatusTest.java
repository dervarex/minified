package com.dervarex.minified.modrinth.versions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionStatusTest {

    @Test
    void fromApiValue_returnsEnum_whenMatch() {
        assertEquals(VersionStatus.LISTED, VersionStatus.fromApiValue("listed"));
        assertEquals(VersionStatus.ARCHIVED, VersionStatus.fromApiValue("archived"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(VersionStatus.LISTED, VersionStatus.fromApiValue("LISTED"));
    }

    @Test
    void fromApiValue_returnsUnknown_whenNoMatch() {
        assertEquals(VersionStatus.UNKNOWN, VersionStatus.fromApiValue("nope"));
    }

    @Test
    void fromApiValue_returnsNull_whenNull() {
        assertNull(VersionStatus.fromApiValue(null));
    }

    @Test
    void getApiValue_returnsValue() {
        assertEquals("listed", VersionStatus.LISTED.getApiValue());
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("archived", VersionStatus.ARCHIVED.toString());
    }
}