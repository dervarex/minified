package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.VersionSearchOptions;
import com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("manual")
class VersionsClientTest {

    // iris 1.8.2 for 1.21.4, requires sodium joBzVWtR
    private static final String IRIS = "ZnhzDm36";
    private static final String IRIS_SHA1 = "3e3933baa7ba6a94645311e4d183915de45243f0";
    private static final String SODIUM = "joBzVWtR";

    private final VersionsClient versions = Modrinth.connect().versions();

    @Test
    void getsAVersionWithItsFiles() {
        Version iris = versions.get(IRIS);

        assertEquals("1.8.2+1.21.4-fabric", iris.getVersionNumber());
        assertEquals("YL57xq9U", iris.getProjectId());
        assertEquals(IRIS_SHA1, iris.getPrimaryFile().getHashes().get("sha1"));
        assertEquals(2665879, iris.getPrimaryFile().getSize());
        assertEquals(SODIUM, iris.getDependencies().getFirst().getVersionId());
    }

    @Test
    void getsManyVersionsAtOnce() {
        assertEquals(List.of(IRIS, SODIUM), versions.getMany(IRIS, SODIUM).stream().map(Version::getId).toList());
    }

    @Test
    void filtersVersionsOfAProject() {
        List<Version> found = versions.getByProject("iris", VersionSearchOptions.builder()
                .gameVersions("1.21.4")
                .loaders(ModLoader.FABRIC)
                .build());

        assertFalse(found.isEmpty());
        assertTrue(found.stream().allMatch(version -> version.supportsGameVersion("1.21.4") && version.hasLoader(ModLoader.FABRIC)));
    }

    @Test
    void findsAVersionByItsNumber() {
        assertEquals(IRIS, versions.getByNumber("iris", "1.8.2+1.21.4-fabric").getId());
    }

    @Test
    void findsAVersionByFileHash() {
        assertEquals(IRIS, versions.fromHash(IRIS_SHA1).getId());
    }

    @Test
    void findsVersionsByFileHashes() {
        assertEquals(IRIS, versions.fromHashes(IRIS_SHA1).getFirst().getId());
    }

    @Test
    void findsTheNewestVersionsForFileHashes() {
        assertFalse(versions.latestFromHashes(VersionSearchOptions.builder().build(), IRIS_SHA1).isEmpty());
    }

    @Test
    void findsTheNewestVersionForTheSameLoaderAndGameVersion() {
        Version newest = versions.latestFromHashes(VersionSearchOptions.builder().loaders(ModLoader.FABRIC).gameVersions("1.21.4").build(), IRIS_SHA1).getFirst();

        assertTrue(newest.hasLoader(ModLoader.FABRIC));
        assertTrue(newest.supportsGameVersion("1.21.4"));
    }

    @Test
    void latestMatchesGameVersionAndLoader() {
        Version latest = versions.getLatest("iris", VersionSearchOptions.builder()
                .gameVersions("1.21.4")
                .loaders(ModLoader.FABRIC)
                .build());

        assertTrue(latest.supportsGameVersion("1.21.4"));
        assertTrue(latest.hasLoader(ModLoader.FABRIC));
    }

    @Test
    void latestIsNullWhenNothingMatches() {
        assertNull(versions.getLatest("iris", VersionSearchOptions.builder().gameVersions("0.0.1").build()));
    }

    @Test
    void unknownVersionNumbersAreNotFound() {
        assertThrows(ModrinthNotFoundException.class, () -> versions.getByNumber("iris", "no-such-version"));
    }

    @Test
    void limitAndOffsetPage() {
        VersionSearchOptions.Builder options = VersionSearchOptions.builder().limit(2);
        List<Version> first = versions.getByProject("iris", options.build());
        List<Version> second = versions.getByProject("iris", options.offset(1).build());

        assertEquals(2, first.size());
        assertEquals(first.get(1).getId(), second.get(0).getId());
    }
}
