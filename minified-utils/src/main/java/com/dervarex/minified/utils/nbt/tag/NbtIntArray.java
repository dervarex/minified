package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for an int array
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtIntArray(int[] value) implements NbtTag {
    /**
     * {@return 11}
     */
    public byte id() { return 11; }
    public int[] value() { return value; }
    /**
     * {@return a copy with its own array}
     */
    public NbtIntArray copy() { return new NbtIntArray(value.clone()); }
}
