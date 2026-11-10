package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading anything (usually, you want to use its inheritors)
 */
@API(status = API.Status.STABLE)
public class DownloadException extends RuntimeException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public DownloadException(String message) {
        super(message);
    }
    /**
     * Creates the exception without a message
     */
    public DownloadException() {
        super();
    }
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Creates the exception
     *
     * @param cause the original error
     */
    public DownloadException(Throwable cause) {
        super(cause);
    }
}