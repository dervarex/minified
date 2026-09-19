package com.dervarex.minified.modrinth.versions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionTypeTest {

    @Test
    void fromApiValue_returnsEnum_whenMatch() {
        assertEquals(VersionType.RELEASE, VersionType.fromApiValue("release"));
        assertEquals(VersionType.BETA, VersionType.fromApiValue("beta"));
        assertEquals(VersionType.ALPHA, VersionType.fromApiValue("alpha"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(VersionType.RELEASE, VersionType.fromApiValue("RELEASE"));
    }

    @Test
    void fromApiValue_throws_whenNoMatch() {
        assertThrows(IllegalArgumentException.class, () -> VersionType.fromApiValue("nope"));
    }

    @Test
    void fromApiValue_returnsNull_whenNull() {
        assertNull(VersionType.fromApiValue(null));
    }

    @Test
    void getApiValue_returnsValue() {
        assertEquals("release", VersionType.RELEASE.getApiValue());
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("beta", VersionType.BETA.toString());
    }
}