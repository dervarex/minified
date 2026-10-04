package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.os.OS;
import com.dervarex.minified.utils.shortcut.Shortcut;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

// needs powershell and its COM object, so only on windows
@EnabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
class WindowsShortcutWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesALnkFile() throws IOException {
        Path lnk = Shortcut.builder()
                .execPath(Path.of(System.getProperty("java.home"), "bin", "java.exe"))
                .arguments("-version")
                .comment("it's minified")
                .os(OS.WINDOWS)
                .shortcutPath(tempDir.resolve("Minified's Launcher.lnk"))
                .build()
                .write();

        assertTrue(Files.size(lnk) > 0);
    }
}
