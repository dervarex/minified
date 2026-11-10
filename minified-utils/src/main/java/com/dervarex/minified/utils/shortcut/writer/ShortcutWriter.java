package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.os.OS;
import com.dervarex.minified.utils.shortcut.Shortcut;

import java.io.IOException;
import java.nio.file.Path;
import org.apiguardian.api.API;

/**
 * Writes a {@link Shortcut} in the format of one os
 */
@API(status = API.Status.STABLE)
public interface ShortcutWriter {

    /**
     * Writes the shortcut
     *
     * @param shortcut the shortcut
     * @throws IOException if the file can't be written
     * @return where the shortcut got written
     */
    Path write(Shortcut shortcut) throws IOException;

    /**
     * for an unknown os, we'll just use the <a href="https://www.freedesktop.org/">freedesktop</a> .desktop file format, since many other operating systems such as *BSD use it
     *
     * @param os the os
     * @return the writer for it
     */
    static ShortcutWriter forOS(OS os) {
        return switch (os) {
            case WINDOWS -> new WindowsShortcutWriter();
            case MACOS -> new MacShortcutWriter();
            case LINUX, UNKNOWN -> new LinuxShortcutWriter();
        };
    }
}