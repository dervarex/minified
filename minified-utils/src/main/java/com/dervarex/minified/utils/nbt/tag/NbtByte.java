package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a byte
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtByte(byte value) implements NbtTag {
    /**
     * {@return 1}
     */
    public byte id() { return 1; }
    public byte value() { return value; }
}
