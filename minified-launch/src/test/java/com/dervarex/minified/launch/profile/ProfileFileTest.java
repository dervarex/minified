package com.dervarex.minified.launch.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfileFileTest {
    @TempDir
    Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {"", "/etc/passwd", "../evil.jar", "mods/../../evil.jar", "mods//evil.jar", "./mods/a.jar",
            "mods\\evil.jar", "C:/evil.jar", "mods/"})
    void rejectsPathsOutsideTheGameDirectory(String path) {
        assertThrows(IllegalArgumentException.class, () -> ProfileFile.builder(path).build());
    }

    @Test
    void defaultsToARequiredEnabledFile() {
        ProfileFile file = ProfileFile.builder("mods/sodium.jar").build();

        assertEquals("sodium.jar", file.fileName());
        assertEquals(SideRequirement.REQUIRED, file.client());
        assertEquals(SideRequirement.REQUIRED, file.server());
        assertTrue(file.enabled());
        assertEquals(-1, file.size());
        assertEquals(Map.of(), file.hashes());
    }

    @Test
    void readsLocalFiles() throws IOException {
        Path mod = tempDir.resolve("mods/sodium.jar.disabled");
        Files.createDirectories(mod.getParent());
        Files.writeString(mod, "minified");

        ProfileFile file = ProfileFile.fromFile(tempDir, mod);

        assertEquals("mods/sodium.jar", file.path());
        assertFalse(file.enabled());
        assertEquals(8, file.size());
        assertEquals(40, file.hashes().get("sha1").length());
        assertEquals(128, file.hashes().get("sha512").length());
        assertTrue(file.downloads().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> ProfileFile.fromFile(tempDir.resolve("mods"), tempDir.resolve("other.jar")));
    }

    @Test
    void sideRequirementsUseTheMrpackNames() {
        assertEquals("unsupported", SideRequirement.UNSUPPORTED.id());
        assertEquals(SideRequirement.OPTIONAL, SideRequirement.fromId("optional"));
    }
}
