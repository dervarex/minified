package com.dervarex.minified.utils.nbt.tag;

public record NbtBoolean(boolean value) implements NbtTag {
    public byte id() { return 1; }
}
