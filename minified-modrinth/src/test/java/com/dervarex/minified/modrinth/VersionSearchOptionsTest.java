package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.loaders.PluginLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VersionSearchOptionsTest {

    @Test
    void defaults_matchEverything() {
        VersionSearchOptions options = VersionSearchOptions.builder().build();
        assertTrue(options.getGameVersions().isEmpty());
        assertTrue(options.getLoaders().isEmpty());
        assertNull(options.getFeatured());
        assertEquals(0, options.getLimit());
        assertEquals(0, options.getOffset());
    }

    @Test
    void builder_setsEverything() {
        VersionSearchOptions options = VersionSearchOptions.builder()
                .gameVersions("1.21.11", " 1.21.10 ", "", null, "1.21.11")
                .loaders(ModLoader.FABRIC, null, PluginLoader.PAPER, ModLoader.FABRIC)
                .featured(true)
                .limit(5)
                .offset(2)
                .build();
        assertEquals(List.of("1.21.11", "1.21.10"), options.getGameVersions());
        assertEquals(List.of(ModLoader.FABRIC, PluginLoader.PAPER), options.getLoaders());
        assertEquals(Boolean.TRUE, options.getFeatured());
        assertEquals(5, options.getLimit());
        assertEquals(2, options.getOffset());
    }

    @Test
    void nullArrays_clearTheFilter() {
        VersionSearchOptions options = VersionSearchOptions.builder()
                .gameVersions("1.21.11").gameVersions((String[]) null)
                .loaders(ModLoader.FABRIC).loaders((ModLoader[]) null)
                .build();
        assertTrue(options.getGameVersions().isEmpty());
        assertTrue(options.getLoaders().isEmpty());
    }

    @Test
    void negativeValues_throw() {
        assertThrows(IllegalArgumentException.class, () -> VersionSearchOptions.builder().limit(-1));
        assertThrows(IllegalArgumentException.class, () -> VersionSearchOptions.builder().offset(-1));
    }
}
