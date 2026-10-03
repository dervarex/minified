package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.os.OS;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShortcutSupportTest {

    @Test
    void namesComeFromTheFileWithoutItsExtension() {
        assertEquals("minified", ShortcutSupport.deriveName(Path.of("/opt/minified.exe")));
        assertEquals("minified", ShortcutSupport.deriveName(Path.of("/opt/minified")));
        assertEquals(".hidden", ShortcutSupport.deriveName(Path.of("/opt/.hidden")));
    }

    @Test
    void windowsAndMacShortcutsGoOnTheDesktop() {
        Path home = Path.of(System.getProperty("user.home"));

        assertEquals(home.resolve("Desktop/a_b.lnk"), ShortcutSupport.defaultShortcutPath(OS.WINDOWS, "a/b"));
        assertEquals(home.resolve("Desktop/a_b.command"), ShortcutSupport.defaultShortcutPath(OS.MACOS, "a:b"));
    }

    @Test
    void quotingSurvivesApostrophes() {
        assertEquals("'it'\\''s'", ShortcutSupport.shellQuote("it's"));
        assertEquals("'it''s'", ShortcutSupport.psQuote("it's"));
    }
}
