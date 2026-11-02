package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading a native
 */
@API(status = API.Status.STABLE)
public class NativeExtractionException extends LibraryDownloadException {
    public NativeExtractionException(String message) {
        super(message);
    }
    public NativeExtractionException() {
        super();
    }
    public NativeExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
    public NativeExtractionException(Throwable cause) {
        super(cause);
    }
}