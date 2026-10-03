package com.dervarex.minified.utils.nbt.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NbtListTest {

    @Test
    void onlyTakesItsElementType() {
        NbtList list = new NbtList((byte) 3);

        assertThrows(IllegalArgumentException.class, () -> list.add(new NbtString("not an int")));
    }
}
