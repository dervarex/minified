package com.dervarex.minified.launch.exceptions.profile;

import org.apiguardian.api.API;
import com.dervarex.minified.launch.exceptions.download.DownloadException;
import com.dervarex.minified.launch.profile.ProfileFile;
import lombok.Getter;

/**
 * Gets thrown when a file of a profile couldn't be downloaded or its hash doesn't match
 */
@Getter
@API(status = API.Status.EXPERIMENTAL)
public class FailedToInstallProfileFileException extends DownloadException {
    /** The file that failed */
    private final ProfileFile file;

    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param file    the file that failed
     * @param cause   the original error
     */
    public FailedToInstallProfileFileException(String message, ProfileFile file, Throwable cause) {
        super(message, cause);
        this.file = file;
    }

    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param file    the file that failed
     */
    public FailedToInstallProfileFileException(String message, ProfileFile file) {
        super(message);
        this.file = file;
    }
}
