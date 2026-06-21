package com.dervarex.minified.launch.utils;

public class OSUtil {
    public static String getMinecraftOs() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "windows";
        }

        if (os.contains("mac") || os.contains("darwin")) {
            return "osx";
        }

        if (os.contains("linux")
                || os.contains("unix")) {
            return "linux";
        }

        return "unknown";
    }
}
