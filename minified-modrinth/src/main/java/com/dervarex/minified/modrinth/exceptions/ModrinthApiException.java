package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when a request to the Modrinth API fails at the HTTP or protocol level, like when there's no connection.
 * <pre>{@code
 * try {
 *     modrinth.projects().get("this-does-not-exist");
 * } catch (ModrinthApiException e) {
 *     System.out.println("Modrinth request to " + e.getRequestUrl() + " failed: " + e.getStatusCode());
 * }
 * }</pre>
 */
@API(status = API.Status.STABLE)
public class ModrinthApiException extends ModrinthException {
    private final int statusCode;
    private final String requestUrl;

    /**
     * Creates the exception
     *
     * @param message    what went wrong
     * @param statusCode the HTTP status code, -1 if the request never reached the server
     * @param requestUrl the URL of the request
     * @param cause      the exception that caused it
     */
    public ModrinthApiException(String message, int statusCode, String requestUrl, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.requestUrl = requestUrl;
    }

    /**
     * {@return status code given by the modrinth api, -1 if the request never reached the server}
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

