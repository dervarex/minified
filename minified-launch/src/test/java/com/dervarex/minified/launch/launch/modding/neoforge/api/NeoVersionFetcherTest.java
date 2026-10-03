package com.dervarex.minified.launch.launch.modding.neoforge.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoVersionFetcherTest {

    @Test
    void mapsOldMinecraftVersionsWithoutLeadingOne() {
        assertEquals("21.1.", NeoVersionFetcher.toNeoForgeVersionPrefix("1.21.1"));
        assertEquals("21.4.", NeoVersionFetcher.toNeoForgeVersionPrefix("1.21.4"));
        assertEquals("21.0.", NeoVersionFetcher.toNeoForgeVersionPrefix("1.21"));
        assertEquals("20.2.", NeoVersionFetcher.toNeoForgeVersionPrefix("1.20.2"));
    }

    @Test
    void mapsNewMinecraftVersionsWithFullVersion() {
        assertEquals("26.1.0.", NeoVersionFetcher.toNeoForgeVersionPrefix("26.1"));
        assertEquals("26.1.2.", NeoVersionFetcher.toNeoForgeVersionPrefix("26.1.2"));
        assertEquals("26.3.0.", NeoVersionFetcher.toNeoForgeVersionPrefix("26.3"));
    }

    @Test
    void prefixDoesNotMatchOtherMinorVersions() {
        // 21.10.x belongs to 1.21.10
        assertFalse("21.10.64".startsWith(NeoVersionFetcher.toNeoForgeVersionPrefix("1.21.1")));
        assertTrue("21.1.252".startsWith(NeoVersionFetcher.toNeoForgeVersionPrefix("1.21.1")));
    }

    @Test
    void detectsLegacyVersions() {
        assertTrue(NeoVersionFetcher.isLegacyVersion("1.20.1-47.1.106"));
        assertFalse(NeoVersionFetcher.isLegacyVersion("21.1.252"));
        assertFalse(NeoVersionFetcher.isLegacyVersion("26.1.0.20"));
    }
}
