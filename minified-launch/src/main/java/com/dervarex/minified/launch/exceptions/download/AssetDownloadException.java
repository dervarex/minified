package com.dervarex.minified.launch.exceptions.download;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue downloading some assets
 */
@API(status = API.Status.STABLE)
public class AssetDownloadException extends DownloadException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public AssetDownloadException(String message) {
        super(message);
    }
    /**
     * Creates the exception without a message
     */
    public AssetDownloadException() {
        super();
    }
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public AssetDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Creates the exception
     *
     * @param cause the original error
     */
    public AssetDownloadException(Throwable cause) {
        super(cause);
    }
}
