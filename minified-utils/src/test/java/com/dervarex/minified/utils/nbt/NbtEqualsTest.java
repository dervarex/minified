package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.NbtBoolean;
import com.dervarex.minified.utils.nbt.tag.NbtByte;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtInt;
import com.dervarex.minified.utils.nbt.tag.NbtLong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NbtEqualsTest {

    @Test
    void comparesArraysByWhatIsInThem() {
        assertTrue(NbtEquals.deepEquals(compoundWith(new long[]{1, 2}), compoundWith(new long[]{1, 2})));
        assertFalse(NbtEquals.deepEquals(compoundWith(new long[]{1, 2}), compoundWith(new long[]{2, 1})));
    }

    @Test
    void sameValueDifferentTypeIsNotEqual() {
        assertFalse(NbtEquals.deepEquals(new NbtInt(1), new NbtLong(1)));
        assertFalse(NbtEquals.deepEquals(new NbtByte((byte) 0), new NbtBoolean(true)));
    }

    private static NbtCompound compoundWith(long[] value) {
        NbtCompound inner = new NbtCompound();
        inner.setLongArray("BlockStates", value);
        NbtCompound outer = new NbtCompound();
        outer.setCompound("section", inner);
        return outer;
    }

    @Test
    void booleansEqualTheBytesTheyAreStoredAs() {
        assertTrue(NbtEquals.deepEquals(new NbtBoolean(true), new NbtByte((byte) 1)));
        assertTrue(NbtEquals.deepEquals(new NbtByte((byte) 0), new NbtBoolean(false)));
    }
}
