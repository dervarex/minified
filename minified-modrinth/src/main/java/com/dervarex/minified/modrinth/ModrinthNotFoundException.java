package com.dervarex.minified.modrinth;

import org.apiguardian.api.API;

/**
 * Thrown when the Modrinth API returns HTTP 404.
 */
@API(status = API.Status.STABLE)
public final class ModrinthNotFoundException extends ModrinthApiException {
    public ModrinthNotFoundException(String message, int statusCode, String requestUrl, Throwable cause) {
        super(message, statusCode, requestUrl, cause);
    }
}

