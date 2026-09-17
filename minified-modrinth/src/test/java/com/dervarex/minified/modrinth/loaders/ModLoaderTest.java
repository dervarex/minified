package com.dervarex.minified.modrinth.loaders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModLoaderTest {

    @Test
    void getApiValue_returnsExpectedValue() {
        assertEquals("fabric", ModLoader.FABRIC.getApiValue());
        assertEquals("neoforge", ModLoader.NEOFORGE.getApiValue());
        assertEquals("risugamis-modloader", ModLoader.RISUGAMIS_MODLOADER.getApiValue());
    }

    @Test
    void fromApiValue_returnsEnum_whenValueMatches() {
        assertEquals(ModLoader.FABRIC, ModLoader.fromApiValue("fabric"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(ModLoader.FABRIC, ModLoader.fromApiValue("FABRIC"));
        assertEquals(ModLoader.NEOFORGE, ModLoader.fromApiValue("NeoForge"));
    }

    @Test
    void fromApiValue_returnsNull_whenValueIsNull() {
        assertNull(ModLoader.fromApiValue(null));
    }

    @Test
    void fromApiValue_throws_whenValueUnknown() {
        assertThrows(IllegalArgumentException.class, () -> ModLoader.fromApiValue("unknown"));
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("fabric", ModLoader.FABRIC.toString());
        assertEquals("quilt", ModLoader.QUILT.toString());
    }
}