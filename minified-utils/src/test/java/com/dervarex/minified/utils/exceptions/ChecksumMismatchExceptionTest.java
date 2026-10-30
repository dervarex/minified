package com.dervarex.minified.utils.exceptions;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChecksumMismatchExceptionTest {

    @Test
    void messageContainsBothChecksumsWhenKnown() {
        ChecksumMismatchException e = new ChecksumMismatchException("https://example.com/a.zip", "abc", "def");

        assertEquals("Checksum mismatch for https://example.com/a.zip. Expected abc but got def", e.getMessage());
        assertEquals("abc", e.getExpected());
        assertEquals("def", e.getActual());
        assertInstanceOf(IOException.class, e);
    }

    @Test
    void messageStaysShortWhenChecksumsAreUnknown() {
        ChecksumMismatchException e = new ChecksumMismatchException("https://example.com/a.zip");

        assertEquals("Checksum mismatch for https://example.com/a.zip", e.getMessage());
        assertEquals("https://example.com/a.zip", e.getSource());
        assertNull(e.getExpected());
    }
}
