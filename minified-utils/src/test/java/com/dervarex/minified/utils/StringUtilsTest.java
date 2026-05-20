package com.dervarex.minified.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {
    @Test
    void isBlankMatchesNullAndWhitespace() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank("  \t"));
    }

    @Test
    void truncateHandlesBounds() {
        assertNull(StringUtils.truncate(null, 3));
        assertEquals("abc", StringUtils.truncate("abcdef", 3));
    }
}

