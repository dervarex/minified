package com.dervarex.minified.modrinth;

import org.apiguardian.api.API;

/**
 * Thrown when a Modrinth file download fails or fails validation.
 */
@API(status = API.Status.STABLE)
public final class ModrinthDownloadException extends ModrinthException {
    public ModrinthDownloadException(String message) {
        super(message);
    }

    public ModrinthDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}

