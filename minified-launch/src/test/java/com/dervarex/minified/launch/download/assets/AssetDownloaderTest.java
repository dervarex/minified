package com.dervarex.minified.launch.download.assets;

import com.dervarex.minified.launch.TestEnvironment;
import com.dervarex.minified.launch.launch.LaunchContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("manual")
class AssetDownloaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadAssetsSuccessfully() throws Exception {
        Path assetsDir = tempDir.resolve("assets");

        AssetDownloader downloader = new AssetDownloader(10);
        downloader.downloadAssets("1.21.11", assetsDir, new LaunchContext(null, TestEnvironment.config(tempDir)));

        assertTrue(Files.exists(assetsDir), "Assets directory should exist");

        try (var files = Files.walk(assetsDir)) {
            assertTrue(
                    files.anyMatch(Files::isRegularFile),
                    "Assets directory should contain downloaded files"
            );
        }
    }
}
