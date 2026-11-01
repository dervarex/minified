package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading anything (usually, you want to use its inheritors)
 */
@API(status = API.Status.STABLE)
public class DownloadException extends RuntimeException {
    public DownloadException(String message) {
        super(message);
    }
    public DownloadException() {
        super();
    }
    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    public DownloadException(Throwable cause) {
        super(cause);
    }
}