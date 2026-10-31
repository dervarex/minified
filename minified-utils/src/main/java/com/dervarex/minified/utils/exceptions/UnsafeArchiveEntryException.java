package com.dervarex.minified.utils.exceptions;

import org.apiguardian.api.API;

import java.io.IOException;

/**
 * Gets thrown when an archive contains an entry that would be extracted outside the target directory (path traversal, most likely malware)
 */
@API(status = API.Status.EXPERIMENTAL)
public class UnsafeArchiveEntryException extends IOException {

    private static final long serialVersionUID = 1L;

    /** The name of the blocked entry */
    private final String entryName;

    /**
     * Creates the exception for an entry that would end up outside the target directory
     *
     * @param entryName the name of the blocked entry
     */
    public UnsafeArchiveEntryException(String entryName) {
        super("Blocked path traversal entry in archive: " + entryName);
        this.entryName = entryName;
    }

    /**
     * {@return the name of the blocked entry}
     */
    public String getEntryName() {
        return entryName;
    }
}
