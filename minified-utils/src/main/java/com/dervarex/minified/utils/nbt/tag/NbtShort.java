package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a short
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtShort(short value) implements NbtTag {
    /**
     * {@return 2}
     */
    public byte id() { return 2; }
    public short value() { return value; }
}
