package com.dervarex.minified.launch.exceptions.download;

import lombok.Getter;
import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading something over http
 */
@Getter
@API(status = API.Status.STABLE)
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

}