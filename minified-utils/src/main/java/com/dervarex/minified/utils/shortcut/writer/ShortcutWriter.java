package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.os.OS;
import com.dervarex.minified.utils.shortcut.Shortcut;

import java.io.IOException;
import java.nio.file.Path;

public interface ShortcutWriter {

    Path write(Shortcut shortcut) throws IOException;

    /**
     * for an unknown os, we'll just use the <a href="https://www.freedesktop.org/">freedesktop</a> .desktop file format, since many other operating systems such as *BSD use it
     */
    static ShortcutWriter forOS(OS os) {
        return switch (os) {
            case WINDOWS -> new WindowsShortcutWriter();
            case MACOS -> new MacShortcutWriter();
            case LINUX, UNKNOWN -> new LinuxShortcutWriter();
        };
    }
}