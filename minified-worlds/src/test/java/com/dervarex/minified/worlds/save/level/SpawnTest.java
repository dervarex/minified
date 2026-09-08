package com.dervarex.minified.worlds.save.level;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpawnTest {

    @Test
    void testLoadSpawnData() {
        NbtCompound nbt = new NbtCompound();
        nbt.setString("dimension", "minecraft:overworld");
        nbt.setInt("pitch", 15);
        nbt.setInt("yaw", 90);
        nbt.setIntArray("pos", new int[]{10, 64, -20});

        Spawn spawn = Spawn.fromNbt(nbt);

        assertEquals("minecraft:overworld", spawn.getDimension());
        assertEquals(15, spawn.getPitch());
        assertEquals(90, spawn.getYaw());
        assertArrayEquals(new int[]{10, 64, -20}, spawn.getPos());
    }

    @Test
    void testLoadEmptyData() {
        Spawn spawn = Spawn.fromNbt(new NbtCompound());

        assertNull(spawn.getDimension());
        assertEquals(0, spawn.getPitch());
        assertEquals(0, spawn.getYaw());
        assertNull(spawn.getPos());
    }

    @Test
    void testSaveWithoutOptionalFields() {
        Spawn spawn = new Spawn();
        spawn.setPitch(1);
        spawn.setYaw(2);

        NbtCompound nbt = spawn.toNbt();

        assertFalse(nbt.has("dimension"));
        assertFalse(nbt.has("pos"));
        assertEquals(1, nbt.getInt("pitch"));
        assertEquals(2, nbt.getInt("yaw"));
    }

    @Test
    void testSaveAndLoadSpawn() {
        Spawn original = new Spawn();
        original.setDimension("minecraft:the_nether");
        original.setPitch(-10);
        original.setYaw(180);
        original.setPos(new int[]{1, 2, 3});

        Spawn parsed = Spawn.fromNbt(original.toNbt());

        assertEquals(original.getDimension(), parsed.getDimension());
        assertEquals(original.getPitch(), parsed.getPitch());
        assertEquals(original.getYaw(), parsed.getYaw());
        assertArrayEquals(original.getPos(), parsed.getPos());
    }
}