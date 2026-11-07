package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a float
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtFloat(float value) implements NbtTag {
    /**
     * {@return 5}
     */
    public byte id() { return 5; }
    public float value() { return value; }
}
