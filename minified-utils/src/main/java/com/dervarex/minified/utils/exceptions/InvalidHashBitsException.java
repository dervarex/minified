package com.dervarex.minified.utils.exceptions;

public class InvalidHashBitsException extends IllegalArgumentException {
    private final int hashBits;
    public InvalidHashBitsException(int hashBits) {
        super("Invalid SHA hash size: " + hashBits + " (expected a multiple of 8, e.g. 1, 224, 256, 384, 512)");
        this.hashBits = hashBits;
    }
    public int getHashBits() { return hashBits; }
}