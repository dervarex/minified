package com.dervarex.minified.worlds.save.level;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void testLoadEmptyData() {
        Version version = Version.fromNbt(new NbtCompound());

        assertEquals(0, version.getId());
        assertNull(version.getName());
        assertNull(version.getSeries());
        assertFalse(version.isSnapshot());
    }

    @Test
    void testLoadVersionData() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("Id", 3953);
        nbt.setString("Name", "1.21.4");
        nbt.setString("Series", "main");
        nbt.setByte("Snapshot", (byte) 1);

        Version version = Version.fromNbt(nbt);

        assertEquals(3953, version.getId());
        assertEquals("1.21.4", version.getName());
        assertEquals("main", version.getSeries());
        assertTrue(version.isSnapshot());
    }

    @Test
    void testSaveWithoutOptionalFields() {
        Version version = new Version();
        version.setId(1);

        NbtCompound nbt = version.toNbt();

        assertFalse(nbt.has("Name"));
        assertFalse(nbt.has("Series"));
        assertEquals(1, nbt.getInt("Id"));
        assertEquals((byte) 0, nbt.getByte("Snapshot"));
    }

    @Test
    void testSaveAndLoadVersion() {
        Version original = new Version();
        original.setId(3953);
        original.setName("1.21.4");
        original.setSeries("main");
        original.setSnapshot(true);

        Version parsed = Version.fromNbt(original.toNbt());

        assertEquals(original.getId(), parsed.getId());
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getSeries(), parsed.getSeries());
        assertEquals(original.isSnapshot(), parsed.isSnapshot());
    }
}