package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public record NbtDouble(double value) implements NbtTag {
    public byte id() { return 6; }
    public double value() { return value; }
}
