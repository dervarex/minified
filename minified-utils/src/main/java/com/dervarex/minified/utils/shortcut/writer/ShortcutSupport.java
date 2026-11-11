package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.os.OS;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apiguardian.api.API;

/**
 * Helpers for the shortcut writers
 */
@API(status = API.Status.STABLE)
public final class ShortcutSupport {

    private ShortcutSupport() {}

    /**
     * {@return the file name without the extension}
     *
     * @param execPath the program
     */
    public static String deriveName(Path execPath) {
        String fileName = execPath.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    /**
     * {@return the desktop on Windows and macOS, ~/.local/share/applications on Linux}
     *
     * @param os the os
     * @param name the shortcut name
     */
    public static Path defaultShortcutPath(OS os, String name) {
        String safe = sanitize(name);
        Path home = Paths.get(System.getProperty("user.home"));

        return switch (os) {
            case WINDOWS -> home.resolve("Desktop").resolve(safe + ".lnk");
            case MACOS -> home.resolve("Desktop").resolve(safe + ".command");
            case LINUX, UNKNOWN -> home.resolve(".local/share/applications")
                    .resolve(safe.toLowerCase().replace(' ', '-') + ".desktop");
        };
    }

    /**
     * {@return the string with characters that aren't allowed in file names replaced by _}
     *
     * @param s the string
     */
    public static String sanitize(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * {@return the string escaped for a value in a .desktop file}
     *
     * @param s the string
     */
    public static String escapeDesktop(String s) {
        return s.replace("\\", "\\\\").replace("\n", " ");
    }

    /**
     * Quotes an argument for the Exec= key of a .desktop file, see the freedesktop spec
     *
     * @param s the argument
     * @return the quoted argument
     */
    public static String desktopExecQuote(String s) {
        String quoted = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("`", "\\`")
                .replace("$", "\\$");
        // the string-level escaping runs before the quoting, so every backslash has to be doubled once more
        return '"' + quoted.replace("\\", "\\\\").replace("%", "%%") + '"';
    }

    /**
     * {@return the string in single quotes for a shell}
     *
     * @param s the string
     */
    public static String shellQuote(String s) {
        return "'" + s.replace("'", "'\\''") + "'";
    }

    /**
     * {@return the string in single quotes for PowerShell}
     *
     * @param s the string
     */
    public static String psQuote(String s) {
        return "'" + s.replace("'", "''") + "'";
    }
}