package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when the Modrinth API rate limits a request.
 */
@API(status = API.Status.STABLE)
public final class ModrinthRateLimitedException extends ModrinthApiException {
    private final long retryAfterSeconds;

    public ModrinthRateLimitedException(String message, int statusCode, String requestUrl, long retryAfterSeconds, Throwable cause) {
        super(message, statusCode, requestUrl, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

