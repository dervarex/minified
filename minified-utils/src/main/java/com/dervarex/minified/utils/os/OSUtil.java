package com.dervarex.minified.utils.os;

public class OSUtil {
    public static OS getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return OS.WINDOWS;
        } else if (os.contains("mac") || os.contains("darwin")) {
            return OS.MACOS;
        } else if (os.contains("nix") || os.contains("nux")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }
}
