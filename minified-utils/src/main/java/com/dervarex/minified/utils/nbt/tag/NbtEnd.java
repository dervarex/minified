package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class NbtEnd implements NbtTag {
    public static final NbtEnd INSTANCE = new NbtEnd();
    private NbtEnd() {}
    public byte id() { return 0; }
}
