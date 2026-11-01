package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtFloat(float value) implements NbtTag {
    public byte id() { return 5; }
    public float value() { return value; }
}
