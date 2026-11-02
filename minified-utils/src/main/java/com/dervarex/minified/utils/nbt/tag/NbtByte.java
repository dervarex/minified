package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtByte(byte value) implements NbtTag {
    public byte id() { return 1; }
    public byte value() { return value; }
}
