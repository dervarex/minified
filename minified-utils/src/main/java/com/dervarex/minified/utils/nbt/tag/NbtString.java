package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Tag for a string
 *
 * @param value the value
 */
@API(status = API.Status.STABLE)
public record NbtString(String value) implements NbtTag {
    /**
     * {@return 8}
     */
    public byte id() { return 8; }
    public String value() { return value; }
}
