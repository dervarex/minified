package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading a native
 */
@API(status = API.Status.STABLE)
public class NativeExtractionException extends LibraryDownloadException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public NativeExtractionException(String message) {
        super(message);
    }
    /**
     * Creates the exception without a message
     */
    public NativeExtractionException() {
        super();
    }
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public NativeExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Creates the exception
     *
     * @param cause the original error
     */
    public NativeExtractionException(Throwable cause) {
        super(cause);
    }
}