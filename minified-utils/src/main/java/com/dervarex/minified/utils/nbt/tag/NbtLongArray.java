package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a long array
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtLongArray(long[] value) implements NbtTag {
    /**
     * {@return 12}
     */
    public byte id() { return 12; }
    public long[] value() { return value; }
    /**
     * {@return a copy with its own array}
     */
    public NbtLongArray copy() { return new NbtLongArray(value.clone()); }

}
