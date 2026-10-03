package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.os.OS;
import com.dervarex.minified.utils.shortcut.Shortcut;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacShortcutWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesACommandScript() throws IOException {
        Path script = Shortcut.builder()
                .execPath(tempDir.resolve("Notch's Launcher"))
                .arguments("--profile default")
                .directory(tempDir)
                .os(OS.MACOS)
                .shortcutPath(tempDir.resolve("Desktop/Minified.command"))
                .build()
                .write();

        assertEquals("""
                #!/bin/bash
                cd '%1$s'
                exec '%1$s/Notch'\\''s Launcher' --profile default
                """.formatted(tempDir), Files.readString(script));
        assertTrue(Files.isExecutable(script));
    }
}
