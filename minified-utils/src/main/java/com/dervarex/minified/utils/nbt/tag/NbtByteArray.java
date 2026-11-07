package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a byte array
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtByteArray(byte[] value) implements NbtTag {
    /**
     * {@return 7}
     */
    public byte id() { return 7; }
    public byte[] value() { return value; }
    /**
     * {@return a copy with its own array}
     */
    public NbtByteArray copy() { return new NbtByteArray(value.clone()); }
}
