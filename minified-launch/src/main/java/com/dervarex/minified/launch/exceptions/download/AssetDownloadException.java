package com.dervarex.minified.launch.exceptions.download;

/**
 * Gets thrown when there was an issue downloading some assets
 */
public class AssetDownloadException extends DownloadException {
    public AssetDownloadException(String message) {
        super(message);
    }
    public AssetDownloadException() {
        super();
    }
    public AssetDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    public AssetDownloadException(Throwable cause) {
        super(cause);
    }
}
