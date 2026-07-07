package com.dervarex.minified.modrinth;

/**
 * Thrown when the Modrinth API returns HTTP 404.
 */
public final class ModrinthNotFoundException extends ModrinthApiException {
    public ModrinthNotFoundException(String message, int statusCode, String requestUrl, Throwable cause) {
        super(message, statusCode, requestUrl, cause);
    }
}

