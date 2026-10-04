package com.dervarex.minified.launch.download.libraries;

import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import com.dervarex.minified.utils.os.OS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OfflineLibraryValidatorTest {

    private static final VanillaLoader VANILLA = new VanillaLoader("1.21.11");

    @TempDir
    Path tempDir;

    @Test
    void passesWhenEverythingIsCached() throws IOException {
        cacheVersionJson();
        writeLibrary("org/ow2/asm/asm/9.8/asm-9.8.jar", "asm");
        writeLibrary("com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar", "brigadier");

        assertDoesNotThrow(() -> OfflineLibraryValidator.validate("1.21.11", VANILLA, librariesDir()));
    }

    @Test
    void needsTheNetworkWithoutCachedVersionJson() {
        OfflineModeNeedsNetworkException e = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> OfflineLibraryValidator.validate("1.21.11", VANILLA, librariesDir()));

        assertEquals(OfflineModeNeedsNetworkException.Reason.MISSING_VERSION_MANIFEST, e.getReason());
    }

    @Test
    void reportsMissingAndBrokenLibraries() throws IOException {
        cacheVersionJson();
        writeLibrary("org/ow2/asm/asm/9.8/asm-9.8.jar", "not asm at all");

        OfflineModeNeedsNetworkException e = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> OfflineLibraryValidator.validate("1.21.11", VANILLA, librariesDir()));

        assertEquals(OfflineModeNeedsNetworkException.Reason.MISSING_LIBRARIES, e.getReason());
        assertEquals(2, e.getMissingResources().size());
    }

    @Test
    void fabricNeedsItsCachedProfile() throws IOException {
        cacheVersionJson();
        writeLibrary("org/ow2/asm/asm/9.8/asm-9.8.jar", "asm");
        writeLibrary("com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar", "brigadier");

        OfflineModeNeedsNetworkException e = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> OfflineLibraryValidator.validate("1.21.11", new FabricLoader("1.21.11", "0.16.14"), librariesDir()));

        assertEquals(List.of("Missing cached loader profile: " + tempDir.resolve("jar/cache/profiles/fabric/1.21.11.json")), e.getMissingResources());
    }

    private Path librariesDir() {
        return tempDir.resolve("jar/libraries");
    }

    private void cacheVersionJson() throws IOException {
        Path versionJson = tempDir.resolve("jar/cache/versions/1.21.11.json");
        Files.createDirectories(versionJson.getParent());
        Files.writeString(versionJson, """
                { "libraries": [
                    { "downloads": { "artifact": { "path": "org/ow2/asm/asm/9.8/asm-9.8.jar", "sha1": "%s" } } },
                    { "downloads": { "artifact": { "path": "com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar", "sha1": "%s" } } },
                    {
                      "rules": [{ "action": "allow", "os": { "name": "%s" } }],
                      "downloads": { "artifact": { "path": "some/other/os/only.jar", "sha1": "%s" } }
                    }
                ] }
                """.formatted(sha1("asm"), sha1("brigadier"), otherOs(), sha1("nope")));
    }

    private void writeLibrary(String path, String content) throws IOException {
        Path library = librariesDir().resolve(path);
        Files.createDirectories(library.getParent());
        Files.writeString(library, content);
    }

    private static String otherOs() {
        return OS.getCurrentOS() == OS.WINDOWS ? "linux" : "windows";
    }

    private static String sha1(String content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void fabricLibrariesAreCheckedToo() throws IOException {
        cacheVersionJson();
        writeLibrary("org/ow2/asm/asm/9.8/asm-9.8.jar", "asm");
        writeLibrary("com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar", "brigadier");
        Path profile = Files.createDirectories(tempDir.resolve("jar/cache/profiles/fabric")).resolve("1.21.11.json");
        Files.writeString(profile, """
                { "libraries": [
                    { "name": "net.fabricmc:fabric-loader:0.16.14", "url": "https://maven.fabricmc.net/" },
                    { "name": "net.fabricmc:intermediary:1.21.11", "url": "https://maven.fabricmc.net/", "sha1": "%s" }
                ] }
                """.formatted(sha1("intermediary")));
        writeLibrary("net/fabricmc/intermediary/1.21.11/intermediary-1.21.11.jar", "not intermediary");

        OfflineModeNeedsNetworkException e = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> OfflineLibraryValidator.validate("1.21.11", new FabricLoader("1.21.11", "0.16.14"), librariesDir()));

        // the loader is missing, intermediary is there but broken
        assertEquals(2, e.getMissingResources().size());
    }
}
