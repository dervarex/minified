package com.dervarex.minified.launch.download.assets;

import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetOfflineValidatorTest {

    private static final String ICON = sha1("icon");
    private static final String SOUND = sha1("sound");

    @TempDir
    Path tempDir;

    @Test
    void passesWhenEverythingIsCached() throws IOException {
        cacheVersionJson();
        writeIndex();
        writeObject(ICON, "icon");
        writeObject(SOUND, "sound");

        assertDoesNotThrow(() -> AssetOfflineValidator.validate("1.21.11", assetsDir()));
    }

    @Test
    void needsTheNetworkWithoutCachedVersionJson() {
        OfflineModeNeedsNetworkException e = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> AssetOfflineValidator.validate("1.21.11", assetsDir()));

        assertEquals(OfflineModeNeedsNetworkException.Reason.MISSING_VERSION_MANIFEST, e.getReason());
    }

    @Test
    void needsTheNetworkWithoutAssetIndex() throws IOException {
        cacheVersionJson();

        OfflineModeNeedsNetworkException e = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> AssetOfflineValidator.validate("1.21.11", assetsDir()));

        assertEquals(OfflineModeNeedsNetworkException.Reason.MISSING_ASSETS, e.getReason());
    }

    @Test
    void reportsMissingAndCorruptedAssets() throws IOException {
        cacheVersionJson();
        writeIndex();
        writeObject(ICON, "definitely not the icon");

        OfflineModeNeedsNetworkException e = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> AssetOfflineValidator.validate("1.21.11", assetsDir()));

        assertEquals(OfflineModeNeedsNetworkException.Reason.MISSING_ASSETS, e.getReason());
        assertEquals(2, e.getMissingResources().size());
    }

    private Path assetsDir() {
        return tempDir.resolve("assets");
    }

    private void cacheVersionJson() throws IOException {
        Path versionJson = tempDir.resolve("cache/versions/1.21.11.json");
        Files.createDirectories(versionJson.getParent());
        Files.writeString(versionJson, "{ \"assetIndex\": { \"id\": \"29\" } }");
    }

    private void writeIndex() throws IOException {
        Path index = assetsDir().resolve("indexes/29.json");
        Files.createDirectories(index.getParent());
        Files.writeString(index, """
                { "objects": {
                    "icons/icon_16x16.png": { "hash": "%s", "size": 4 },
                    "sounds/ambient/cave1.ogg": { "hash": "%s", "size": 5 }
                } }
                """.formatted(ICON, SOUND));
    }

    private void writeObject(String hash, String content) throws IOException {
        Path object = assetsDir().resolve("objects").resolve(hash.substring(0, 2)).resolve(hash);
        Files.createDirectories(object.getParent());
        Files.writeString(object, content);
    }

    private static String sha1(String content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
