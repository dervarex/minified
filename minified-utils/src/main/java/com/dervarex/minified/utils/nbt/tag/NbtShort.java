package com.dervarex.minified.utils.nbt.tag;

public record NbtShort(short value) implements NbtTag {
    public byte id() { return 2; }
    public short value() { return value; }
}
