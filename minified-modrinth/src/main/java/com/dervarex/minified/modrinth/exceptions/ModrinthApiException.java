package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Base class for HTTP and protocol level API errors.
 */
@API(status = API.Status.STABLE)
public class ModrinthApiException extends ModrinthException {
    private final int statusCode;
    private final String requestUrl;

    public ModrinthApiException(String message, int statusCode, String requestUrl, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.requestUrl = requestUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRequestUrl() {
        return requestUrl;
    }
}

