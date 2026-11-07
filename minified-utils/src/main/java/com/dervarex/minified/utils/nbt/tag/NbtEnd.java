package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Marks the end of a compound, use {@link #INSTANCE}
 */
@API(status = API.Status.STABLE)
public final class NbtEnd implements NbtTag {
    /**
     * The only instance
     */
    public static final NbtEnd INSTANCE = new NbtEnd();
    private NbtEnd() {}
    /**
     * {@return 0}
     */
    public byte id() { return 0; }
}
