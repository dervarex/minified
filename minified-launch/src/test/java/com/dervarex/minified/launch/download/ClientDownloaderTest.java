package com.dervarex.minified.launch.download;

import com.dervarex.minified.launch.TestEnvironment;
import com.dervarex.minified.launch.launch.LaunchContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("manual")
class ClientDownloaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadClientJarSuccessfully() throws Exception {
        Path clientJar = tempDir.resolve("client.jar");

        ClientDownloader downloader = new ClientDownloader();
        downloader.downloadClient("1.21.11", clientJar, new LaunchContext(null, TestEnvironment.config(tempDir)));

        assertTrue(Files.exists(clientJar), "Client jar should exist");
        assertTrue(Files.size(clientJar) > 1024, "Client jar should not be empty");
    }
}
