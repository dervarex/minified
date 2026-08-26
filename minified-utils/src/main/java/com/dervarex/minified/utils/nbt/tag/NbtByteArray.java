package com.dervarex.minified.utils.nbt.tag;

public record NbtByteArray(byte[] value) implements NbtTag {
    public byte id() { return 7; }
    public byte[] value() { return value; }
}
