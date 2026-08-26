package com.dervarex.minified.utils.nbt.tag;

public record NbtLong(long value) implements NbtTag {
    public byte id() { return 4; }
    public long value() { return value; }
}
