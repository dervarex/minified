package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a boolean, saved as a byte because NBT doesn't have booleans
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtBoolean(boolean value) implements NbtTag {
    /**
     * {@return 1, same as a byte}
     */
    public byte id() { return 1; }
    public boolean value() { return value; }
}
