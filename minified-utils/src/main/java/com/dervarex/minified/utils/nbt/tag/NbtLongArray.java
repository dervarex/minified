package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtLongArray(long[] value) implements NbtTag {
    public byte id() { return 12; }
    public long[] value() { return value; }
    public NbtLongArray copy() { return new NbtLongArray(value.clone()); }

}
