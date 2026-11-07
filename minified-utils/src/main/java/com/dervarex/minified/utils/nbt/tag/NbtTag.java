package com.dervarex.minified.utils.nbt.tag;

import org.apiguardian.api.API;

/**
 * Any NBT tag
 */
@API(status = API.Status.STABLE)
public sealed interface NbtTag

        permits NbtCompound, NbtList, NbtString, NbtByte, NbtBoolean, NbtShort, NbtInt,

        NbtLong, NbtFloat, NbtDouble, NbtByteArray, NbtIntArray,

        NbtLongArray, NbtEnd {

    /**
     * {@return the tag id, like 10 for a compound}
     */
    byte id();

    /**
     * Deep copy, tags that can't change anyway just return themselves
     *
     * @return the copy
     */
    default NbtTag copy() {
        return this;
    }

}
