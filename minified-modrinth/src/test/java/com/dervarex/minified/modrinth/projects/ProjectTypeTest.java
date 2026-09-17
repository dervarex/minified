package com.dervarex.minified.modrinth.projects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTypeTest {

    @Test
    void fromApiValue_returnsEnum_whenMatch() {
        assertEquals(ProjectType.MOD, ProjectType.fromApiValue("mod"));
        assertEquals(ProjectType.MODPACK, ProjectType.fromApiValue("modpack"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(ProjectType.MOD, ProjectType.fromApiValue("MOD"));
    }

    @Test
    void fromApiValue_throws_whenUnknown() {
        assertThrows(IllegalArgumentException.class, () -> ProjectType.fromApiValue("not_real"));
    }

    @Test
    void fromApiValue_returnsNull_whenNull() {
        assertNull(ProjectType.fromApiValue(null));
    }

    @Test
    void getApiValue_returnsValue() {
        assertEquals("mod", ProjectType.MOD.getApiValue());
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("modpack", ProjectType.MODPACK.toString());
    }
}