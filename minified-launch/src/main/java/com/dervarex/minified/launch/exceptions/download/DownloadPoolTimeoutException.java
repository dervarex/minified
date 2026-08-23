package com.dervarex.minified.launch.exceptions.download;

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
