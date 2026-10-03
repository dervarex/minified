package com.dervarex.minified.launch.launch.modding.neoforge.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeoInstallerFetcherTest {

    @Test
    void legacyVersionsStillLiveUnderTheForgeName() {
        assertEquals(
                "https://maven.neoforged.net/releases/net/neoforged/forge/1.20.1-47.1.106/forge-1.20.1-47.1.106-installer.jar",
                NeoInstallerFetcher.getInstallerLink("1.20.1-47.1.106")
        );
        assertEquals(
                "https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.252/neoforge-21.1.252-installer.jar",
                NeoInstallerFetcher.getInstallerLink("21.1.252")
        );
    }
}
