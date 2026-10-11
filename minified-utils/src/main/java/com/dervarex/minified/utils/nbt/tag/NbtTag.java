package com.dervarex.minified.utils.nbt.tag;

public sealed interface NbtTag

        permits NbtCompound, NbtList, NbtString, NbtByte, NbtBoolean, NbtShort, NbtInt,

        NbtLong, NbtFloat, NbtDouble, NbtByteArray, NbtIntArray,

        NbtLongArray, NbtEnd {

    byte id();

    /**
     * Deep copy, tags that can't change anyway just return themselves
     */
    default NbtTag copy() {
        return this;
    }

}
