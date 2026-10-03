package com.dervarex.minified.launch.launch.modding.neoforge.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeoVersionJsonTest {

    @TempDir
    Path tempDir;

    @Test
    void findsWhatTheInstallerWrote() throws IOException {
        install("neoforge-21.1.252", "neoforge-21.1.252");

        assertEquals("neoforge-21.1.252", NeoVersionJson.getVersionJson(tempDir, "21.1.252").getString("id"));
    }

    @Test
    void findsLegacyVersionsUnderTheirForgeName() throws IOException {
        install("1.20.1-forge-47.1.106", "1.20.1-forge-47.1.106");

        assertEquals("1.20.1-forge-47.1.106", NeoVersionJson.getVersionJson(tempDir, "1.20.1-47.1.106").getString("id"));
    }

    @Test
    void findsRenamedFoldersByTheirId() throws IOException {
        install("my-cool-instance", "neoforge-21.1.252");

        assertEquals("neoforge-21.1.252", NeoVersionJson.getVersionJson(tempDir, "21.1.252").getString("id"));
    }

    private void install(String folder, String id) throws IOException {
        Path versionJson = Files.createDirectories(tempDir.resolve("versions").resolve(folder)).resolve(folder + ".json");
        Files.writeString(versionJson, "{ \"id\": \"" + id + "\" }");
    }
}
