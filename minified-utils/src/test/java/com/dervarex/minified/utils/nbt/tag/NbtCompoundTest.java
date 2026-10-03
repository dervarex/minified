package com.dervarex.minified.utils.nbt.tag;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NbtCompoundTest {

    @Test
    void gettersOnlyReturnTheirOwnType() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("Score", 1);

        assertEquals(1, nbt.getInt("Score"));
        assertThrows(NoSuchElementException.class, () -> nbt.getLong("Score"));
        assertThrows(NoSuchElementException.class, () -> nbt.getInt("Missing"));
    }

    @Test
    void booleansCanBeReadFromBytes() {
        NbtCompound nbt = new NbtCompound();
        nbt.setByte("hardcore", (byte) 1);

        assertTrue(nbt.getBoolean("hardcore"));
    }

    @Test
    void asMapIsReadOnly() {
        NbtCompound nbt = new NbtCompound();

        assertThrows(UnsupportedOperationException.class, () -> nbt.asMap().put("sneaky", new NbtInt(1)));
    }
}
