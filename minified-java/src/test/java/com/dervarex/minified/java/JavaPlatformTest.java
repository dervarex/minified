package com.dervarex.minified.java;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaPlatformTest {
    @Test
    void majorVersionIsReasonable() {
        assertTrue(JavaPlatform.majorVersion() >= 8);
    }
}

