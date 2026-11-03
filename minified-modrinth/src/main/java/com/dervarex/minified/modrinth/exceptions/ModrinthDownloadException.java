package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when a Modrinth file download fails or fails validation: the file has no download URL, the server answers
 * with a non-2xx status, writing the file fails, or the file doesn't match its hash
 * ({@link ModrinthHashMismatchException})
 */
@API(status = API.Status.STABLE)
public class ModrinthDownloadException extends ModrinthException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public ModrinthDownloadException(String message) {
        super(message);
    }

    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the exception that caused it
     */
    public ModrinthDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}

