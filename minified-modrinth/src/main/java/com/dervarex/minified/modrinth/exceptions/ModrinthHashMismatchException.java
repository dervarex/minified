package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when a downloaded Modrinth file doesn't match the hash Modrinth told us
 */
@API(status = API.Status.STABLE)
public final class ModrinthHashMismatchException extends ModrinthDownloadException {
    private final String filename;
    private final String algorithm;
    private final String expectedHash;
    private final String actualHash;

    public ModrinthHashMismatchException(String filename, String algorithm, String expectedHash, String actualHash) {
        super("Hash verification failed for " + filename + " using " + algorithm);
        this.filename = filename;
        this.algorithm = algorithm;
        this.expectedHash = expectedHash;
        this.actualHash = actualHash;
    }

    /**
     * {@return the name of the file that doesn't match the hash}
     */
    public String getFilename() {
        return filename;
    }

    /**
     * {@return the hashing algorithm used}
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * {@return the expected hash, from modrinth}
     */
    public String getExpectedHash() {
        return expectedHash;
    }

    /**
     * {@return the actual hash of the file}
     */
    public String getActualHash() {
        return actualHash;
    }
}
