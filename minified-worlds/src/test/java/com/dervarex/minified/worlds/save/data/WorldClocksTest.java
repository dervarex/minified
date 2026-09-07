package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.NbtEquals;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldClocksTest {

    @Test
    void testLoadEmptyData() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 1);
        WorldClocks clocks = WorldClocks.fromNbt(nbt);
        assertTrue(clocks.getClocks().isEmpty());
    }

    @Test
    void testLoadSkipsInvalidData() {
        NbtCompound withoutTicks = new NbtCompound();
        withoutTicks.setString("unrelated", "value");
        NbtCompound data = new NbtCompound();
        data.setCompound("minecraft:overworld", withoutTicks);
        NbtCompound nbt = new NbtCompound();
        nbt.setCompound("data", data);
        nbt.setInt("DataVersion", 1);

        WorldClocks clocks = WorldClocks.fromNbt(nbt);

        assertTrue(clocks.getClocks().isEmpty());
    }

    @Test
    void testSaveAndLoadWorldClocks() {
        WorldClocks original = new WorldClocks();
        original.setDataVersion(3953);
        original.getClocks().put("minecraft:overworld", 1234L);

        WorldClocks parsed = WorldClocks.fromNbt(original.toNbt());

        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(Long.valueOf(1234L), parsed.getClocks().get("minecraft:overworld"));
    }

    @Test
    void testSaveTwiceMatches() {
        WorldClocks original = new WorldClocks();
        original.getClocks().put("minecraft:the_nether", 42L);

        NbtCompound first = original.toNbt();
        NbtCompound second = WorldClocks.fromNbt(first).toNbt();
        assertTrue(NbtEquals.deepEquals(first, second));
    }
}
