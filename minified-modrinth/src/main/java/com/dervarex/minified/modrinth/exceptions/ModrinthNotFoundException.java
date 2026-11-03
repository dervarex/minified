package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when the Modrinth API returns HTTP 404.
 */
@API(status = API.Status.STABLE)
public final class ModrinthNotFoundException extends ModrinthApiException {
    /**
     * Creates the exception
     *
     * @param message    what went wrong
     * @param statusCode the HTTP status code, 404
     * @param requestUrl the URL of the request
     * @param cause      the exception that caused it
     */
    public ModrinthNotFoundException(String message, int statusCode, String requestUrl, Throwable cause) {
        super(message, statusCode, requestUrl, cause);
    }
}
