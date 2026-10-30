package com.dervarex.minified.java.exceptions;

import java.io.IOException;

/**
 * Gets thrown when the Java runtime archive couldn't be downloaded
 */
public class JavaRuntimeDownloadException extends IOException {

    private static final long serialVersionUID = 1L;

    private final String url;
    private final int statusCode;

    /**
     * @param message    what went wrong
     * @param url        the URL of the archive
     * @param statusCode the HTTP status code, or -1 if there was no response
     * @param cause      the original error, or null
     */
    public JavaRuntimeDownloadException(String message, String url, int statusCode, Throwable cause) {
        super(message, cause);
        this.url = url;
        this.statusCode = statusCode;
    }

    /**
     * @return the URL of the archive
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the HTTP status code, or -1 if there was no response
     */
    public int getStatusCode() {
        return statusCode;
    }
}
