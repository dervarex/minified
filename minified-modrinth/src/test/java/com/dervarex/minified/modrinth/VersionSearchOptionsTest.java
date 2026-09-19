package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionSearchOptionsTest {

    @Test
    void builder_gameVersions_trimsAndDistincts() {
        VersionSearchOptions options = VersionSearchOptions.builder()
                .gameVersions(" 1.20 ", "1.20", "1.21")
                .build();
        assertArrayEquals(new String[]{"1.20", "1.21"}, options.gameVersions);
    }

    @Test
    void builder_loadersString_normalizes() {
        VersionSearchOptions options = VersionSearchOptions.builder()
                .loaders(" fabric ", "neoforge")
                .build();
        assertArrayEquals(new String[]{"fabric", "neoforge"}, options.loaders);
    }

    @Test
    void builder_loadersModLoader_convertsToStrings() {
        VersionSearchOptions options = VersionSearchOptions.builder()
                .loaders(ModLoader.FABRIC, ModLoader.NEOFORGE)
                .build();
        assertArrayEquals(new String[]{"fabric", "neoforge"}, options.loaders);
    }

    @Test
    void builder_loadersModLoader_skipsNulls() {
        VersionSearchOptions options = VersionSearchOptions.builder()
                .loaders(ModLoader.FABRIC, null, ModLoader.NEOFORGE)
                .build();
        assertArrayEquals(new String[]{"fabric", "neoforge"}, options.loaders);
    }

    @Test
    void builder_featured_setsValue() {
        VersionSearchOptions options = VersionSearchOptions.builder().featured(true).build();
        assertEquals(Boolean.TRUE, options.featured);
    }

    @Test
    void builder_limit_usesZeroForNegative() {
        VersionSearchOptions options = VersionSearchOptions.builder().limit(-1).build();
        assertEquals(0, options.limit);
    }

    @Test
    void builder_offset_usesZeroForNegative() {
        VersionSearchOptions options = VersionSearchOptions.builder().offset(-1).build();
        assertEquals(0, options.offset);
    }

    @Test
    void defaultLimit_isTen() {
        assertEquals(10, new VersionSearchOptions().limit);
    }

    @Test
    void defaultOffset_isZero() {
        assertEquals(0, new VersionSearchOptions().offset);
    }
}