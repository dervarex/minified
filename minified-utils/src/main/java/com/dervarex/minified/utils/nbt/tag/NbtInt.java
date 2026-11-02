package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtInt(int value) implements NbtTag {
    public byte id() { return 3; }
    public int value() { return value; }
}
