package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtShort(short value) implements NbtTag {
    public byte id() { return 2; }
    public short value() { return value; }
}
