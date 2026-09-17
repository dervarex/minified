package com.dervarex.minified.modrinth.loaders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginLoaderTest {

    @Test
    void getApiValue_returnsExpectedValue() {
        assertEquals("paper", PluginLoader.PAPER.getApiValue());
        assertEquals("sponge", PluginLoader.SPONGE.getApiValue());
    }

    @Test
    void fromApiValue_returnsEnum_whenValueMatches() {
        assertEquals(PluginLoader.PAPER, PluginLoader.fromApiValue("paper"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(PluginLoader.SPIGOT, PluginLoader.fromApiValue("SPIGOT"));
    }

    @Test
    void fromApiValue_returnsNull_whenValueIsNull() {
        assertNull(PluginLoader.fromApiValue(null));
    }

    @Test
    void fromApiValue_throws_whenValueUnknown() {
        assertThrows(IllegalArgumentException.class, () -> PluginLoader.fromApiValue("unknown"));
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("paper", PluginLoader.PAPER.toString());
    }
}