package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading a library or native.
 * <br>
 * If you want to catch issues downloading natives on its own, please use the {@link NativeExtractionException} instead
 */
@API(status = API.Status.STABLE)
public class LibraryDownloadException extends DownloadException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public LibraryDownloadException(String message) {
        super(message);
    }
    /**
     * Creates the exception without a message
     */
    public LibraryDownloadException() {
        super();
    }
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public LibraryDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Creates the exception
     *
     * @param cause the original error
     */
    public LibraryDownloadException(Throwable cause) {
        super(cause);
    }
}
