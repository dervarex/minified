package com.dervarex.minified.utils.exceptions;

import org.apiguardian.api.API;

import java.nio.file.Path;

/**
 * Gets thrown when the downloading thread was interrupted
 */
@API(status = API.Status.EXPERIMENTAL)
public class DownloadInterruptedException extends DownloadFailedException {

    private static final long serialVersionUID = 1L;

    /**
     * @param url   the URL that was downloaded
     * @param path  where the file should have been saved
     * @param cause the original {@link InterruptedException}
     */
    public DownloadInterruptedException(String url, Path path, InterruptedException cause) {
        super("Interrupted while downloading " + url, url, path, cause);
    }
}
