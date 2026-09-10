package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.os.OS;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ShortcutSupport {

    private ShortcutSupport() {}

    public static String deriveName(Path execPath) {
        String fileName = execPath.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

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

    public static String sanitize(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public static String escapeDesktop(String s) {
        return s.replace("\\", "\\\\").replace("\n", " ");
    }

    public static String shellQuote(String s) {
        return "'" + s.replace("'", "'\\''") + "'";
    }

    public static String psQuote(String s) {
        return "'" + s.replace("'", "''") + "'";
    }
}