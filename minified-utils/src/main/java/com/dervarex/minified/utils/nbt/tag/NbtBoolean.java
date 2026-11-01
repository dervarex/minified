package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtBoolean(boolean value) implements NbtTag {
    public byte id() { return 1; }
    public boolean value() { return value; }
}
