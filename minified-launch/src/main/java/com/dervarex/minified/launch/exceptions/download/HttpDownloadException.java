package com.dervarex.minified.launch.exceptions.download;

import lombok.Getter;
import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading something over http
 */
@Getter
@API(status = API.Status.STABLE)
public class HttpDownloadException extends DownloadException {

    /** The HTTP status code */
    private final int statusCode;

    /**
     * Creates the exception
     *
     * @param message    what went wrong
     * @param statusCode the HTTP status code
     */
    public HttpDownloadException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Creates the exception
     *
     * @param message    what went wrong
     * @param statusCode the HTTP status code
     * @param cause      the original error
     */
    public HttpDownloadException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}