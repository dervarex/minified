package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void getters_returnSetValues() {
        Version version = new Version();
        version.id = "v1";
        version.projectId = "p1";
        version.authorId = "a1";
        version.name = "Version 1";
        version.versionNumber = "1.0.0";
        version.featured = true;
        version.downloads = 42L;
        version.published = Instant.parse("2024-01-01T00:00:00Z");

        assertEquals("v1", version.getId());
        assertEquals("p1", version.getProjectId());
        assertEquals("a1", version.getAuthorId());
        assertEquals("Version 1", version.getName());
        assertEquals("1.0.0", version.getVersionNumber());
        assertTrue(version.isFeatured());
        assertEquals(42L, version.getDownloads());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), version.getPublished());
    }

    @Test
    void getPrimaryFile_returnsPrimaryFile() {
        Version version = new Version();
        VersionFile first = new VersionFile();
        first.primary = false;
        VersionFile second = new VersionFile();
        second.primary = true;
        version.files = new VersionFile[]{first, second};
        assertEquals(second, version.getPrimaryFile());
    }

    @Test
    void getPrimaryFile_returnsFirst_whenNoPrimary() {
        Version version = new Version();
        VersionFile first = new VersionFile();
        first.primary = false;
        VersionFile second = new VersionFile();
        second.primary = false;
        version.files = new VersionFile[]{first, second};
        assertEquals(first, version.getPrimaryFile());
    }

    @Test
    void getPrimaryFile_returnsNull_whenNoFiles() {
        Version version = new Version();
        assertNull(version.getPrimaryFile());
    }

    @Test
    void getPrimaryFile_returnsNull_whenFilesEmpty() {
        Version version = new Version();
        version.files = new VersionFile[0];
        assertNull(version.getPrimaryFile());
    }

    @Test
    void hasLoaderString_returnsTrue_whenMatch() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertTrue(version.hasLoader("fabric"));
    }

    @Test
    void hasLoaderString_ignoresCase() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertTrue(version.hasLoader("FABRIC"));
    }

    @Test
    void hasLoaderString_returnsFalse_whenNoMatch() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertFalse(version.hasLoader("forge"));
    }

    @Test
    void hasLoaderString_returnsFalse_whenNull() {
        Version version = new Version();
        assertFalse(version.hasLoader((String) null));
    }

    @Test
    void hasLoader_ModLoader_returnsTrue() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertTrue(version.hasLoader(ModLoader.FABRIC));
    }

    @Test
    void hasLoader_ModLoader_returnsFalse_whenLoaderNull() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertFalse(version.hasLoader((ModLoader) null));
    }

    @Test
    void supportsVersion_returnsTrue_whenMatch() {
        Version version = new Version();
        version.gameVersions = new String[]{"1.20"};
        assertTrue(version.supportsVersion("1.20"));
    }

    @Test
    void supportsVersion_ignoresCase() {
        Version version = new Version();
        version.gameVersions = new String[]{"1.20"};
        assertTrue(version.supportsVersion("1.20"));
    }

    @Test
    void supportsVersion_returnsFalse_whenNoMatch() {
        Version version = new Version();
        version.gameVersions = new String[]{"1.20"};
        assertFalse(version.supportsVersion("1.19"));
    }

    @Test
    void supportsVersion_returnsFalse_whenNull() {
        Version version = new Version();
        assertFalse(version.supportsVersion(null));
    }
}