package com.dervarex.minified.launch;

import com.dervarex.minified.launch.download.ClientDownloader;
import com.dervarex.minified.launch.download.ServerDownloader;
import com.dervarex.minified.launch.download.assets.AssetDownloader;
import com.dervarex.minified.launch.download.libraries.LibraryDownloader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class DownloaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadClientJarSuccessfully() throws Exception {
        Path clientJar = tempDir.resolve("client.jar");

        ClientDownloader downloader = new ClientDownloader();
        downloader.downloadClient("1.21.11", clientJar);

        assertTrue(Files.exists(clientJar), "Client jar should exist");
        assertTrue(Files.size(clientJar) > 1024, "Client jar should not be empty");
    }

    @Test
    void shouldDownloadServerJarSuccessfully() throws Exception {
        Path serverJar = tempDir.resolve("server.jar");

        ServerDownloader downloader = new ServerDownloader();
        downloader.downloadServer("1.21.11", serverJar);

        assertTrue(Files.exists(serverJar), "Server jar should exist");
        assertTrue(Files.size(serverJar) > 1024, "Server jar should not be empty");
    }

    @Test
    void shouldDownloadLibrariesSuccessfully() throws Exception {
        Path libsDir = tempDir.resolve("libs");

        LibraryDownloader downloader = new LibraryDownloader(10);
        downloader.downloadLibraries("1.21.11", libsDir);

        assertTrue(Files.exists(libsDir), "Libraries directory should exist");

        try (var files = Files.walk(libsDir)) {
            assertTrue(
                    files.anyMatch(Files::isRegularFile),
                    "Libraries directory should contain downloaded files"
            );
        }
    }

    @Test
    void shouldDownloadAssetsSuccessfully() throws Exception {
        Path assetsDir = tempDir.resolve("assets");

        AssetDownloader downloader = new AssetDownloader(10);
        downloader.downloadAssets("1.21.11", assetsDir);

        assertTrue(Files.exists(assetsDir), "Assets directory should exist");

        try (var files = Files.walk(assetsDir)) {
            assertTrue(
                    files.anyMatch(Files::isRegularFile),
                    "Assets directory should contain downloaded files"
            );
        }
    }
}