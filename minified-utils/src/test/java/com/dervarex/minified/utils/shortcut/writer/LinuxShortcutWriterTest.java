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

class LinuxShortcutWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesADesktopEntry() throws IOException {
        Path desktop = Shortcut.builder()
                .name("Minified")
                .comment("Launches\nMinecraft")
                .execPath(tempDir.resolve("minified.sh"))
                .arguments("--profile default")
                .directory(tempDir)
                .icon(tempDir.resolve("icon.png"))
                .os(OS.LINUX)
                .shortcutPath(tempDir.resolve("applications/minified.desktop"))
                .build()
                .write();

        assertEquals("""
                [Desktop Entry]
                Type=Application
                Name=Minified
                Comment=Launches Minecraft
                Exec="%1$s/minified.sh" --profile default
                Path=%1$s
                Icon=%1$s/icon.png
                Terminal=false
                Categories=Utility;
                """.formatted(tempDir), Files.readString(desktop));
        assertTrue(Files.isExecutable(desktop));
    }
}
