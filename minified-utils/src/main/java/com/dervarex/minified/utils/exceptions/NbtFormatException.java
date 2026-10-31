package com.dervarex.minified.utils.exceptions;

import org.apiguardian.api.API;

import java.io.IOException;

/**
 * Gets thrown when NBT data is malformed or uses something that isn't supported, your file probably is corrupt
 * Other {@link IOException}s mean the file couldn't be read or written at all
 */
@API(status = API.Status.EXPERIMENTAL)
public class NbtFormatException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message what the problem with it is
     */
    public NbtFormatException(String message) {
        super(message);
    }
}
