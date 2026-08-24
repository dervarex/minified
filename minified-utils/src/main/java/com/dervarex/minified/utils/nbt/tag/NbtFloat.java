package com.dervarex.minified.utils.nbt.tag;

public record NbtFloat(float value) implements NbtTag {
    public byte id() { return 5; }
}
