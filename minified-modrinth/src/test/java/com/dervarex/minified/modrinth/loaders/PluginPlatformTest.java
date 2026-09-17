package com.dervarex.minified.modrinth.loaders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginPlatformTest {

    @Test
    void getApiValue_returnsExpectedValue() {
        assertEquals("bungeecord", PluginPlatform.BUNGEECORD.getApiValue());
        assertEquals("geyserextension", PluginPlatform.GEYSER_EXTENSION.getApiValue());
        assertEquals("velocity", PluginPlatform.VELOCITY.getApiValue());
        assertEquals("waterfall", PluginPlatform.WATERFALL.getApiValue());
    }

    @Test
    void fromApiValue_returnsEnum_whenValueMatches() {
        assertEquals(PluginPlatform.BUNGEECORD, PluginPlatform.fromApiValue("bungeecord"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(PluginPlatform.VELOCITY, PluginPlatform.fromApiValue("VELOCITY"));
    }

    @Test
    void fromApiValue_returnsNull_whenValueIsNull() {
        assertNull(PluginPlatform.fromApiValue(null));
    }

    @Test
    void fromApiValue_throws_whenValueUnknown() {
        assertThrows(IllegalArgumentException.class, () -> PluginPlatform.fromApiValue("unknown"));
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("velocity", PluginPlatform.VELOCITY.toString());
    }
}