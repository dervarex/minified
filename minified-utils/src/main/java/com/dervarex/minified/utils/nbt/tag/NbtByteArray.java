package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtByteArray(byte[] value) implements NbtTag {
    public byte id() { return 7; }
    public byte[] value() { return value; }
    public NbtByteArray copy() { return new NbtByteArray(value.clone()); }
}
