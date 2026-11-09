package com.dervarex.minified.launch.exceptions.profile;

import org.apiguardian.api.API;
import lombok.Getter;

/**
 * The profile was saved by a newer version of minified, with a format this version can't read
 */
@Getter
@API(status = API.Status.EXPERIMENTAL)
public class UnsupportedProfileFormatException extends FailedToLoadProfileException {
    /** The format version of the file */
    private final int formatVersion;

    /**
     * Creates the exception
     *
     * @param message       what went wrong
     * @param formatVersion the format version of the file
     */
    public UnsupportedProfileFormatException(String message, int formatVersion) {
        super(message);
        this.formatVersion = formatVersion;
    }
}
