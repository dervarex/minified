package com.dervarex.minified.utils.exceptions;

import org.apiguardian.api.API;

import java.nio.file.Path;

/**
 * Gets thrown when a file couldn't be downloaded. If the server answered with an error, the cause is an {@link HttpException}
 */
@API(status = API.Status.EXPERIMENTAL)
public class DownloadFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The URL that was downloaded */
    private final String url;
    /** Where the file should have been saved */
    private final transient Path path;

    /**
     * Creates the exception for a failed download
     *
     * @param message what went wrong
     * @param url     the URL that was downloaded
     * @param path    where the file should have been saved
     * @param cause   the original error
     */
    public DownloadFailedException(String message, String url, Path path, Throwable cause) {
        super(message, cause);
        this.url = url;
        this.path = path;
    }

    /**
     * {@return the URL that was downloaded}
     */
    public String getUrl() {
        return url;
    }

    /**
     * {@return where the file should have been saved}
     */
    public Path getPath() {
        return path;
    }

    /**
     * {@return the HTTP status code if the server answered with an error, otherwise -1}
     */
    public int getStatusCode() {
        return getCause() instanceof HttpException http ? http.getStatusCode() : -1;
    }
}
