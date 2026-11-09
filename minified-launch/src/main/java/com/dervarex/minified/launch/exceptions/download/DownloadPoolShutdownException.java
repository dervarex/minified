package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue shutting down the downloading pool
 */
@API(status = API.Status.STABLE)
public class DownloadPoolShutdownException extends DownloadException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public DownloadPoolShutdownException(String message) {
        super(message);
    }
    /**
     * Creates the exception without a message
     */
    public DownloadPoolShutdownException() {
        super();
    }
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public DownloadPoolShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Creates the exception
     *
     * @param cause the original error
     */
    public DownloadPoolShutdownException(Throwable cause) {
        super(cause);
    }
}