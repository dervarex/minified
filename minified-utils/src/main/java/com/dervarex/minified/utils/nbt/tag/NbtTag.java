package com.dervarex.minified.utils.nbt.tag;

public sealed interface NbtTag

        permits NbtCompound, NbtList, NbtString, NbtByte, NbtBoolean, NbtShort, NbtInt,

        NbtLong, NbtFloat, NbtDouble, NbtByteArray, NbtIntArray,

        NbtLongArray, NbtEnd {

    byte id();

}
