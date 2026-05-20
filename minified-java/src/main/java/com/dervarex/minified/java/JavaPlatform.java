package com.dervarex.minified.java;

public final class JavaPlatform {
    private JavaPlatform() {
    }

    public static int majorVersion() {
        return Runtime.version().feature();
    }

    public static boolean isAtLeast(int version) {
        return majorVersion() >= version;
    }
}

