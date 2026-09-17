package com.dervarex.minified.java;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaPlatformTest {
    @Test
    void majorVersionIsReasonable() {
        assertTrue(JavaPlatform.majorVersion() >= 8);
    }

    @Test
    void isAtLeast_returnsTrue_whenSameAsCurrentMajor() {
        int current = JavaPlatform.majorVersion();
        assertTrue(JavaPlatform.isAtLeast(current));
    }

    @Test
    void isAtLeast_returnsTrue_whenLowerThanCurrentMajor() {
        int current = JavaPlatform.majorVersion();
        if (current > 0) {
            assertTrue(JavaPlatform.isAtLeast(current - 1));
        }
    }

    @Test
    void isAtLeast_returnsFalse_whenHigherThanCurrentMajor() {
        int current = JavaPlatform.majorVersion();
        assertFalse(JavaPlatform.isAtLeast(current + 1));
    }
}

