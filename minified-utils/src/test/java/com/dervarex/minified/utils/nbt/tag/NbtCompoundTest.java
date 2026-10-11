package com.dervarex.minified.utils.nbt.tag;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void removeGivesBackWhatWasThere() {
        NbtCompound nbt = new NbtCompound();
        nbt.setString("LevelName", "New World");

        assertEquals(new NbtString("New World"), nbt.remove("LevelName").orElseThrow());
        assertFalse(nbt.has("LevelName"));
        assertTrue(nbt.remove("LevelName").isEmpty());
    }

    @Test
    void copiesDontShareAnything() {
        NbtCompound spawn = new NbtCompound();
        spawn.setIntArray("pos", new int[]{1, 64, 1});
        NbtList brands = new NbtList((byte) 8);
        brands.add(new NbtString("vanilla"));
        NbtCompound original = new NbtCompound();
        original.setCompound("spawn", spawn);
        original.setList("ServerBrands", brands);
        original.setLongArray("data", new long[]{42});
        original.setInt("Time", 1);

        NbtCompound copy = original.copy();
        copy.getCompound("spawn").getIntArray("pos")[1] = 100;
        copy.getCompound("spawn").setString("dimension", "minecraft:the_nether");
        copy.getList("ServerBrands").add(new NbtString("fabric"));
        copy.getLongArray("data")[0] = 0;
        copy.setInt("Time", 2);

        assertArrayEquals(new int[]{1, 64, 1}, spawn.getIntArray("pos"));
        assertFalse(spawn.has("dimension"));
        assertEquals(1, original.getList("ServerBrands").size());
        assertArrayEquals(new long[]{42}, original.getLongArray("data"));
        assertEquals(1, original.getInt("Time"));
        assertEquals(List.copyOf(original.asMap().keySet()), List.copyOf(copy.asMap().keySet()));
    }
}
