package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when the Modrinth API rate limits a request with HTTP 429. {@link #getRetryAfterSeconds()} says how long
 * to wait:
 * <pre>{@code
 * try {
 *     modrinth.projects().search(request);
 * } catch (ModrinthRateLimitedException e) {
 *     Thread.sleep(e.getRetryAfterSeconds() * 1000L);
 * }
 * }</pre>
 */
@API(status = API.Status.STABLE)
public final class ModrinthRateLimitedException extends ModrinthApiException {
    private final long retryAfterSeconds;

    /**
     * Creates the exception
     *
     * @param message           what went wrong
     * @param statusCode        the HTTP status code, 429
     * @param requestUrl        the URL of the request
     * @param retryAfterSeconds how long to wait before trying again, 0 if Modrinth didn't say
     * @param cause             the exception that caused it
     */
    public ModrinthRateLimitedException(String message, int statusCode, String requestUrl, long retryAfterSeconds, Throwable cause) {
        super(message, statusCode, requestUrl, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * {@return how long we have to wait to retry (source: modrinth), 0 if Modrinth didn't say}
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

