package com.dervarex.minified.utils.version;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionListProviderTest {

    @Test
    void listsEverythingFromRubyDungToToday() throws Exception {
        List<String> versions = VersionListProvider.getVersions();

        assertTrue(versions.contains("rd-132211"));
        assertTrue(versions.contains("1.21.11"));
        assertTrue(versions.contains(VersionListProvider.getLatestSnapshotVersion()));
    }

    @Test
    void releasesLeaveOutSnapshotsAndTheOldStuff() throws Exception {
        List<String> releases = VersionListProvider.getReleaseVersions();

        assertTrue(releases.contains("1.21.11"));
        assertTrue(releases.contains(VersionListProvider.getLatestReleaseVersion()));
        assertFalse(releases.contains("24w14a"));
        assertFalse(releases.contains("b1.7.3"));
        assertFalse(releases.contains("rd-132211"));
    }
}
