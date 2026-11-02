package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue shutting down the downloading pool
 */
@API(status = API.Status.STABLE)
public class DownloadPoolShutdownException extends DownloadException {
    public DownloadPoolShutdownException(String message) {
        super(message);
    }
    public DownloadPoolShutdownException() {
        super();
    }
    public DownloadPoolShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
    public DownloadPoolShutdownException(Throwable cause) {
        super(cause);
    }
}