package com.dervarex.minified.utils.nbt.tag;

public record NbtIntArray(int[] value) implements NbtTag {
    public byte id() { return 11; }
    public int[] value() { return value; }
    public NbtIntArray copy() { return new NbtIntArray(value.clone()); }
}
