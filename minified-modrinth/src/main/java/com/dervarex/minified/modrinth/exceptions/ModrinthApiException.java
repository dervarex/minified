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

    /**
     * {@return status code given by the modrinth api}
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * {@return url used for the request}
     */
    public String getRequestUrl() {
        return requestUrl;
    }
}

