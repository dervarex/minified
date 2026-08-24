package com.dervarex.minified.utils.nbt.tag;

public record NbtString(String value) implements NbtTag {
    public byte id() { return 8; }
}
