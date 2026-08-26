package com.dervarex.minified.utils.nbt.tag;

public record NbtLongArray(long[] value) implements NbtTag {
    public byte id() { return 12; }
    public long[] value() { return value; }

}
