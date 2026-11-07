package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a long
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtLong(long value) implements NbtTag {
    /**
     * {@return 4}
     */
    public byte id() { return 4; }
    public long value() { return value; }
}
