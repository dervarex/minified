package com.dervarex.minified.utils.nbt.tag;

import org.junit.jupiter.api.Test;

import java.util.List;
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

    @Test
    void keepsTheOrderThingsWerePutIn() {
        NbtCompound nbt = new NbtCompound();
        for (String key : new String[]{"Data", "LevelName", "Version", "GameRules", "Player", "SpawnX", "SpawnY", "SpawnZ"}) {
            nbt.setInt(key, 1);
        }

        assertEquals(List.of("Data", "LevelName", "Version", "GameRules", "Player", "SpawnX", "SpawnY", "SpawnZ"), List.copyOf(nbt.asMap().keySet()));
    }

    @Test
    void nullIsRefusedRightAway() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> new NbtCompound().put("LevelName", null));

        assertTrue(e.getMessage().contains("LevelName"));
    }
}
