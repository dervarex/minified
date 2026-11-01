package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtString(String value) implements NbtTag {
    public byte id() { return 8; }
    public String value() { return value; }
}
