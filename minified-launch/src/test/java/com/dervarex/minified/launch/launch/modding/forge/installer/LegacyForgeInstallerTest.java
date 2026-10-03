package com.dervarex.minified.launch.launch.modding.forge.installer;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.utils.json.JsonFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyForgeInstallerTest {

    private static final String LOADER_VERSION = "1.7.10-10.13.4.1614-1.7.10";

    // the same shape as the install_profile.json of the 1.7.10 installer, minus most of the libraries
    private static final String INSTALL_PROFILE = """
            {
              "install": {
                "path": "net.minecraftforge:forge:1.7.10-10.13.4.1614-1.7.10",
                "filePath": "forge-1.7.10-10.13.4.1614-1.7.10-universal.jar",
                "stripMeta": true
              },
              "versionInfo": {
                "id": "1.7.10-Forge10.13.4.1614-1.7.10",
                "libraries": [
                  { "name": "net.minecraftforge:forge:1.7.10-10.13.4.1614-1.7.10" },
                  { "name": "org.scala-lang:scala-compiler:2.11.1", "serverreq": true, "clientreq": false },
                  { "name": "org.lwjgl.lwjgl:lwjgl-platform:2.9.1", "natives": { "linux": "natives-linux" } },
                  { "name": "com.google.guava:guava:17.0" }
                ]
              }
            }
            """;

    @TempDir
    Path tempDir;

    @Test
    void recognizesOnlyV1Installers() throws IOException {
        assertTrue(LegacyForgeInstaller.isLegacyInstaller(zip("legacy.jar", Map.of("install_profile.json", INSTALL_PROFILE))));
        assertFalse(LegacyForgeInstaller.isLegacyInstaller(zip("modern.jar", Map.of("install_profile.json", "{ \"spec\": 1, \"profile\": \"forge\" }"))));
        assertFalse(LegacyForgeInstaller.isLegacyInstaller(zip("not-an-installer.jar", Map.of("hello.txt", "hi"))));
    }

    @Test
    void installsWithoutRunningTheInstaller() throws IOException {
        Path installer = zip("forge-installer.jar", Map.of(
                "install_profile.json", INSTALL_PROFILE,
                "forge-1.7.10-10.13.4.1614-1.7.10-universal.jar", "forge"
        ));
        Path guava = Files.createDirectories(tempDir.resolve("libraries/com/google/guava/guava/17.0")).resolve("guava-17.0.jar");
        Files.writeString(guava, "guava");

        LaunchConfiguration config = new LaunchConfiguration.Builder()
                .jarFile(tempDir.resolve("jar/client.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .loader(new ForgeLoader("1.7.10", LOADER_VERSION))
                .build();

        LegacyForgeInstaller.install(installer, config);

        Path universal = tempDir.resolve("jar/libraries/net/minecraftforge/forge/" + LOADER_VERSION + "/forge-" + LOADER_VERSION + ".jar");
        assertEquals("forge", Files.readString(universal));

        JsonFile versionJson = new JsonFile(tempDir.resolve("jar/versions/1.7.10-forge-10.13.4.1614-1.7.10/1.7.10-forge-10.13.4.1614-1.7.10.json"));
        assertEquals("1.7.10-Forge10.13.4.1614-1.7.10", versionJson.getString("id"));
        assertTrue(versionJson.getBoolean(LegacyForgeInstaller.STRIP_META_KEY));
    }

    @Test
    void artifactPathsKeepTheClassifier() {
        assertEquals("org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-linux.jar", LegacyForgeInstaller.toArtifactPath("org.lwjgl:lwjgl:3.3.3:natives-linux"));
    }

    private Path zip(String name, Map<String, String> entries) throws IOException {
        Path path = tempDir.resolve(name);
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(path))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return path;
    }
}
