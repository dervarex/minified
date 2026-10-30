package com.dervarex.minified.utils.exceptions;

import org.apiguardian.api.API;

import java.io.IOException;

/**
 * Gets thrown when a downloaded file doesn't match its expected checksum, so it's corrupt or not the file that was asked for
 */
@API(status = API.Status.EXPERIMENTAL)
public class ChecksumMismatchException extends IOException {

    private static final long serialVersionUID = 1L;

    private final String source;
    private final String expected;
    private final String actual;

    /**
     * @param source   the URL or path of the file
     * @param expected the expected checksum, or null if unknown
     * @param actual   the actual checksum, or null if unknown
     */
    public ChecksumMismatchException(String source, String expected, String actual) {
        super(expected != null && actual != null
                ? "Checksum mismatch for " + source + ". Expected " + expected + " but got " + actual
                : "Checksum mismatch for " + source);
        this.source = source;
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * @param source the URL or path of the file
     */
    public ChecksumMismatchException(String source) {
        this(source, null, null);
    }

    /**
     * @return the URL or path of the file
     */
    public String getSource() {
        return source;
    }

    /**
     * @return the expected checksum, or null if unknown
     */
    public String getExpected() {
        return expected;
    }

    /**
     * @return the actual checksum, or null if unknown
     */
    public String getActual() {
        return actual;
    }
}
