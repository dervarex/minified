package com.dervarex.minified.utils.os;

public enum OS {
    WINDOWS("windows"),
    LINUX("linux"),
    MACOS("osx"),
    UNKNOWN("unknown");

    private final String name;

    OS(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static OS getCurrentOS() {
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