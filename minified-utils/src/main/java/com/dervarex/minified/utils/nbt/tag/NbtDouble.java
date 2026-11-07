package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a double
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtDouble(double value) implements NbtTag {
    /**
     * {@return 6}
     */
    public byte id() { return 6; }
    public double value() { return value; }
}
