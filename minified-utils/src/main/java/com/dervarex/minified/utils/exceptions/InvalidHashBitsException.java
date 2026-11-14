package com.dervarex.minified.utils.exceptions;

/**
 * Gets thrown when the given hash size is invalid. <br>
 * Hash size must be a multiple of 8 bits, like 1, 244, 256, 512 or 1024.
 */
public class InvalidHashBitsException extends IllegalArgumentException {
    private final int hashBits;
    public InvalidHashBitsException(int hashBits) {
        super("Invalid SHA hash size: " + hashBits + " (expected a multiple of 8 bits, e.g. 1, 224, 256, 384, 512)");
        this.hashBits = hashBits;
    }
    public int getHashBits() { return hashBits; }
}