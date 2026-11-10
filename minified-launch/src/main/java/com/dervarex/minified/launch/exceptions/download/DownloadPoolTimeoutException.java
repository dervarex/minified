package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when the download pool timeout was hit
 */
@API(status = API.Status.STABLE)
public class DownloadPoolTimeoutException extends DownloadException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public DownloadPoolTimeoutException(String message) {
        super(message);
    }
    /**
     * Creates the exception without a message
     */
    public DownloadPoolTimeoutException() {
        super();
    }
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public DownloadPoolTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Creates the exception
     *
     * @param cause the original error
     */
    public DownloadPoolTimeoutException(Throwable cause) {
        super(cause);
    }
}
