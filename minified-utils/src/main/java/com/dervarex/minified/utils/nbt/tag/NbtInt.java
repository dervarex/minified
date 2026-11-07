package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for an int
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtInt(int value) implements NbtTag {
    /**
     * {@return 3}
     */
    public byte id() { return 3; }
    public int value() { return value; }
}
