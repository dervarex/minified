package com.dervarex.minified.launch.download.libraries;

import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("manual")
class LibraryDownloaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadLibrariesSuccessfully() throws Exception {
        Path libsDir = tempDir.resolve("libs");

        LibraryDownloader downloader = new LibraryDownloader(10);
        downloader.downloadLibraries(new VanillaLoader("1.21.11"), libsDir);

        assertTrue(Files.exists(libsDir), "Libraries directory should exist");

        try (var files = Files.walk(libsDir)) {
            assertTrue(
                    files.anyMatch(Files::isRegularFile),
                    "Libraries directory should contain downloaded files"
            );
        }
    }
}
