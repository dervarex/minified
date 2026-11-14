package com.dervarex.minified.utils.exceptions;

/**
 * Gets thrown when the given Hashing Algorithm is not supported by the JVM
 */
public class UnsupportedHashAlgorithmException extends RuntimeException {
    private final String algorithm;
    public UnsupportedHashAlgorithmException(String algorithm, Throwable cause) {
        super("Hash algorithm not supported by this JVM: " + algorithm, cause);
        this.algorithm = algorithm;
    }
    public String getAlgorithm() { return algorithm; }
}