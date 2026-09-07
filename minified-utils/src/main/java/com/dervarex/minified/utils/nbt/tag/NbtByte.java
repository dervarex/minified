package com.dervarex.minified.utils.nbt.tag;

public record NbtByte(byte value) implements NbtTag {
    public byte id() { return 1; }
    public byte value() { return value; }
}
