package com.dervarex.minified.utils.nbt.tag;

public record NbtDouble(double value) implements NbtTag {
    public byte id() { return 6; }
}
