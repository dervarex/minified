package com.dervarex.minified.launch.exceptions.download;

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