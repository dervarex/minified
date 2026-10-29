package com.dervarex.minified.launch.exceptions.download;

/**
 * Gets thrown when there was an issue downloading a native
 */
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