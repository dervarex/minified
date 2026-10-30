package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when a downloaded Modrinth file doesn't match the hash Modrinth told us
 */
@API(status = API.Status.EXPERIMENTAL)
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

    public String getFilename() {
        return filename;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getExpectedHash() {
        return expectedHash;
    }

    public String getActualHash() {
        return actualHash;
    }
}
