package com.dervarex.minified.launch.exceptions.libraries;

import com.dervarex.minified.launch.launch.modding.Loader;
import lombok.Getter;
import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue loading the libraries.
 * <br>
 * If you want to catch an error downloading the libraries, please use the {@link com.dervarex.minified.launch.exceptions.download.LibraryDownloadException} instead.
 */
@Getter
@API(status = API.Status.STABLE)
public class FailedToLoadLibrariesException extends RuntimeException {
    /** The loader the libraries were loaded for */
    private final Loader loader;

    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param loader  the loader
     */
    public FailedToLoadLibrariesException(String message, Loader loader) {
        super(message);
        this.loader = loader;
    }

    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param loader  the loader
     * @param cause   the original error
     */
    public FailedToLoadLibrariesException(String message, Loader loader, Throwable cause) {
        super(message, cause);
        this.loader = loader;
    }
}
