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
}