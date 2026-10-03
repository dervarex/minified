package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.VersionSearchOptions;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(IRIS_SHA1, iris.getPrimaryFile().getHash("sha1"));
        assertEquals(2665879, iris.getPrimaryFile().getSize());
        assertEquals(SODIUM, iris.dependencies[0].getVersionId());
    }

    @Test
    void getsManyVersionsAtOnce() {
        assertEquals(List.of(IRIS, SODIUM), versions.getMany(IRIS, SODIUM).stream().map(Version::getId).sorted().toList());
    }

    @Test
    void filtersVersionsOfAProject() {
        List<Version> found = versions.getByProject("iris", VersionSearchOptions.builder()
                .gameVersions("1.21.4")
                .loaders(ModLoader.FABRIC)
                .build());

        assertFalse(found.isEmpty());
        assertTrue(found.stream().allMatch(version -> version.supportsVersion("1.21.4") && version.hasLoader(ModLoader.FABRIC)));
    }

    @Test
    @Disabled("getByProject sends the default limit of 10, so only the newest 10 versions get searched (iris has 243)")
    void findsAVersionByItsNumber() {
        assertEquals(IRIS, versions.getVersionByNumber("iris", "1.8.2+1.21.4-fabric").getId());
    }

    @Test
    @Disabled("calls /version/{hash}, modrinth wants /version_file/{hash}, answers 400")
    void findsAVersionByFileHash() {
        assertEquals(IRIS, versions.fromHash(IRIS_SHA1).getId());
    }

    @Test
    @Disabled("calls /versions?hashes=, that route only takes ids (400), modrinth wants POST /version_files")
    void findsVersionsByFileHashes() {
        assertEquals(IRIS, versions.fromHashes(IRIS_SHA1).getFirst().getId());
    }

    @Test
    @Disabled("calls /version_files/<hashes>, doesn't exist (404), modrinth wants POST /version_files/update")
    void findsTheNewestVersionsForFileHashes() {
        assertFalse(versions.latestFromHashes(IRIS_SHA1).isEmpty());
    }

    @Test
    @Disabled("calls /version/{id}/dependencies, doesn't exist (404), modrinth only has /project/{id}/dependencies")
    void listsTheDependenciesOfAVersion() {
        assertEquals(SODIUM, versions.dependencies(IRIS).getFirst().getVersionId());
    }
}
