package com.dervarex.minified.launch.exceptions.libraries;

import com.dervarex.minified.launch.launch.modding.Loader;
import lombok.Getter;

/**
 * Gets thrown when there was an issue loading the libraries.
 * <br>
 * If you want to catch an error downloading the libraries, please use the {@link com.dervarex.minified.launch.exceptions.download.LibraryDownloadException} instead.
 */
@Getter
public class FailedToLoadLibrariesException extends RuntimeException {
    private final Loader loader;

    public FailedToLoadLibrariesException(String message, Loader loader) {
        super(message);
        this.loader = loader;
    }

    public FailedToLoadLibrariesException(String message, Loader loader, Throwable cause) {
        super(message, cause);
        this.loader = loader;
    }
}
