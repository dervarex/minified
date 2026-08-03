package com.dervarex.minified.servers;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.servers.events.download.server.DownloadServerJarEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("manual")
public class DownloaderTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadServerJarSuccessfully() throws Exception {
        Path serverJar = tempDir.resolve("server.jar");
        EventBus eventBus = new EventBus();


        eventBus.subscribe(DownloadServerJarEvent.class, event -> {
            System.out.printf(
                    "\rDownloading Server Jar | %.2f%% | %d/%d bytes",
                    event.progress() * 100,
                    event.downloadedBytes(),
                    event.totalBytes()
            );
            System.out.flush();
        });
        ServerDownloader downloader = new ServerDownloader();
        downloader.downloadServer("1.21.11", serverJar, eventBus);



        assertTrue(Files.exists(serverJar), "Server jar should exist");
        assertTrue(Files.size(serverJar) > 1024, "Server jar should not be empty");
    }
}
