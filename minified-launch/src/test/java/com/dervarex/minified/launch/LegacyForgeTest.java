package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.launch.launch.modding.forge.installer.LegacyForgeGameJar;
import com.dervarex.minified.launch.launch.modding.forge.installer.LegacyForgeInstaller;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LegacyForgeTest {

    @TempDir
    Path tempDir;

    @Test
    void versionIdKeepsTheBranchSuffix() {
        assertEquals("1.7.10-forge-10.13.4.1614-1.7.10", ForgeVersionJson.getVersionId("1.7.10-10.13.4.1614-1.7.10"));
        assertEquals("1.21.11-forge-61.1.8", ForgeVersionJson.getVersionId("1.21.11-61.1.8"));
    }

    @Test
    void newerForgeUsesTheVanillaJar() {
        LaunchConfiguration config = config("1.12.2", "1.12.2-14.23.5.2864");

        assertNull(LegacyForgeGameJar.resolve(new JsonFile("{}").asObject(), config));
    }

    @Test
    void stripsTheSignatureOfTheGameJar() throws IOException {
        LaunchConfiguration config = config("1.5.2", "1.5.2-7.8.1.738");
        writeZip(tempDir.resolve("jar/client.jar"), Map.of(
                "META-INF/MOJANG_C.SF", "signature",
                "net/minecraft/client/Minecraft.class", "vanilla"
        ));
        JsonObject profile = new JsonFile("{ \"" + LegacyForgeInstaller.STRIP_META_KEY + "\": true }").asObject();

        LegacyForgeGameJar.prepare(profile, config);

        Path gameJar = LegacyForgeGameJar.resolve(profile, config);
        assertEquals(tempDir.resolve("jar/versions/1.5.2-forge-7.8.1.738/1.5.2-forge-7.8.1.738.jar"), gameJar);
        assertEquals(Map.of("net/minecraft/client/Minecraft.class", "vanilla"), readZip(gameJar));
    }

    @Test
    void jarModsReplaceVanillaClasses() throws IOException {
        LaunchConfiguration config = config("1.4.7", "1.4.7-6.6.2.534");
        writeZip(tempDir.resolve("jar/client.jar"), Map.of(
                "META-INF/MANIFEST.MF", "manifest",
                "net/minecraft/client/Minecraft.class", "vanilla",
                "terrain.png", "terrain"
        ));
        writeZip(tempDir.resolve("jar/libraries/forge-universal.zip"), Map.of(
                "net/minecraft/client/Minecraft.class", "forge",
                "net/minecraftforge/common/MinecraftForge.class", "forge"
        ));
        JsonObject profile = new JsonFile("{ \"" + LegacyForgeGameJar.JAR_MODS_KEY + "\": [\"libraries/forge-universal.zip\"] }").asObject();

        LegacyForgeGameJar.prepare(profile, config);

        assertEquals(Map.of(
                "net/minecraft/client/Minecraft.class", "forge",
                "net/minecraftforge/common/MinecraftForge.class", "forge",
                "terrain.png", "terrain"
        ), readZip(LegacyForgeGameJar.resolve(profile, config)));
    }

    private LaunchConfiguration config(String mcVersion, String loaderVersion) {
        return new LaunchConfiguration.Builder()
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("jar/libraries"))
                .jarFile(tempDir.resolve("jar/client.jar"))
                .loader(new ForgeLoader(mcVersion, loaderVersion))
                .build();
    }

    private static void writeZip(Path path, Map<String, String> entries) throws IOException {
        Files.createDirectories(path.getParent());
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(path))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
    }

    private static Map<String, String> readZip(Path path) throws IOException {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            Map<String, String> entries = new HashMap<>();
            for (ZipEntry entry : Collections.list(zip.entries())) {
                entries.put(entry.getName(), new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
            }
            return entries;
        }
    }
}
