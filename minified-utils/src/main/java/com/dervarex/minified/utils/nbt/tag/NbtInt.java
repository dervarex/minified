package com.dervarex.minified.utils.nbt.tag;

public record NbtInt(int value) implements NbtTag {
    public byte id() { return 3; }
    public int value() { return value; }
}
