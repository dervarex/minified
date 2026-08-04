package com.dervarex.minified.launch.exceptions.download;

public class HttpDownloadException extends DownloadException {

    private final int statusCode;

    public HttpDownloadException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpDownloadException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}