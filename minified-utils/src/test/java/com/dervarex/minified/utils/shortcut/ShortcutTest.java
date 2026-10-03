package com.dervarex.minified.utils.shortcut;

import com.dervarex.minified.utils.os.OS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShortcutTest {

    @TempDir
    Path tempDir;

    @Test
    void fillsInNameAndPathFromTheExecutable() {
        String originalHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tempDir.toString());

            Shortcut shortcut = Shortcut.builder()
                    .execPath(Path.of("/opt/minified/Minified Launcher.sh"))
                    .os(OS.LINUX)
                    .build();

            assertEquals("Minified Launcher", shortcut.name());
            assertEquals(tempDir.resolve(".local/share/applications/minified-launcher.desktop"), shortcut.shortcutPath());
        } finally {
            System.setProperty("user.home", originalHome);
        }
    }

    @Test
    void needsSomethingToStart() {
        assertThrows(IllegalStateException.class, () -> Shortcut.builder().name("Nothing").build());
    }
}
