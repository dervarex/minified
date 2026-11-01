package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading a library or native.
 * <br>
 * If you want to catch issues downloading natives on its own, please use the {@link NativeExtractionException} instead
 */
@API(status = API.Status.STABLE)
public class LibraryDownloadException extends DownloadException {
    public LibraryDownloadException(String message) {
        super(message);
    }
    public LibraryDownloadException() {
        super();
    }
    public LibraryDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    public LibraryDownloadException(Throwable cause) {
        super(cause);
    }
}
