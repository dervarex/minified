package com.dervarex.minified.modrinth;

/**
 * Thrown when the Modrinth API rate limits a request.
 */
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

