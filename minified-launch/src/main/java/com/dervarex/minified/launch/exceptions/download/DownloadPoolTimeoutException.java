package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when the download pool timeout was hit
 */
@API(status = API.Status.STABLE)
public class DownloadPoolTimeoutException extends DownloadException {
    public DownloadPoolTimeoutException(String message) {
        super(message);
    }
    public DownloadPoolTimeoutException() {
        super();
    }
    public DownloadPoolTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
    public DownloadPoolTimeoutException(Throwable cause) {
        super(cause);
    }
}
