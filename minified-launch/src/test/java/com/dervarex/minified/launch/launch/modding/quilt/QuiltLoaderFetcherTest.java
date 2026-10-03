package com.dervarex.minified.launch.launch.modding.quilt;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuiltLoaderFetcherTest {

    @Test
    void selectsNewestStableVersionFromUnsortedList() {
        List<String> versions = List.of(
                "0.20.0-beta.9",
                "0.24.0",
                "0.31.0-beta.4",
                "0.30.1",
                "0.30.1-beta.4",
                "0.9.2"
        );

        assertEquals("0.30.1", QuiltLoaderFetcher.selectLatestLoaderVersion(versions));
    }

    @Test
    void comparesVersionPartsNumerically() {
        assertEquals("0.10.0", QuiltLoaderFetcher.selectLatestLoaderVersion(List.of("0.9.9", "0.10.0", "0.2.0")));
    }

    @Test
    void fallsBackToNewestPreReleaseWithoutStableVersions() {
        List<String> versions = List.of("0.1.0-beta.2", "0.1.0-beta.10", "0.1.0-beta.9");

        assertEquals("0.1.0-beta.10", QuiltLoaderFetcher.selectLatestLoaderVersion(versions));
    }

    @Test
    void throwsWithoutVersions() {
        assertThrows(IllegalArgumentException.class, () -> QuiltLoaderFetcher.selectLatestLoaderVersion(List.of()));
    }
}
