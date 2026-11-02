package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtLong(long value) implements NbtTag {
    public byte id() { return 4; }
    public long value() { return value; }
}
