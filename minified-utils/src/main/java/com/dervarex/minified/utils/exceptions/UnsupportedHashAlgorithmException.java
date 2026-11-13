package com.dervarex.minified.utils.exceptions;

public class UnsupportedHashAlgorithmException extends RuntimeException {
    private final String algorithm;
    public UnsupportedHashAlgorithmException(String algorithm, Throwable cause) {
        super("Hash algorithm not supported by this JVM: " + algorithm, cause);
        this.algorithm = algorithm;
    }
    public String getAlgorithm() { return algorithm; }
}