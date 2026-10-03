package com.dervarex.minified.utils.os;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OSTest {

    @ParameterizedTest
    @CsvSource({
            "Windows 11, WINDOWS",
            "Mac OS X, MACOS",
            "Linux, LINUX",
            "FreeBSD, UNKNOWN"
    })
    void detectsWhatTheJvmReports(String osName, OS expected) {
        String original = System.getProperty("os.name");
        try {
            System.setProperty("os.name", osName);
            assertEquals(expected, OS.getCurrentOS());
        } finally {
            System.setProperty("os.name", original);
        }
    }
}
