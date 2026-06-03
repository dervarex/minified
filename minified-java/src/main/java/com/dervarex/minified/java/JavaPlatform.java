package com.dervarex.minified.java;

import com.dervarex.minified.utils.exceptions.HttpException;

import java.io.IOException;

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

