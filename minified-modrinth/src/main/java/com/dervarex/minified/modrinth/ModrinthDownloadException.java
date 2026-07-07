package com.dervarex.minified.modrinth;

/**
 * Thrown when a Modrinth file download fails or fails validation.
 */
public final class ModrinthDownloadException extends ModrinthException {
    public ModrinthDownloadException(String message) {
        super(message);
    }

    public ModrinthDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}

