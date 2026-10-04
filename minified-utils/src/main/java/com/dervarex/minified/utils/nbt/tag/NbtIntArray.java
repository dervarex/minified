package com.dervarex.minified.utils.nbt.tag;

public record NbtIntArray(int[] value) implements NbtTag {
    public byte id() { return 11; }
    public int[] value() { return value; }
}
