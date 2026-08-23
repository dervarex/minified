package com.dervarex.minified.launch.exceptions.download;

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
