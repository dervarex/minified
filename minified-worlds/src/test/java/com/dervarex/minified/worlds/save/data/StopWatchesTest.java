package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StopWatchesTest {

    @Test
    void testLoadStopwatches() {
        NbtCompound stopwatches = new NbtCompound();
        stopwatches.setLong("minecraft:example", 100L);
        NbtCompound data = new NbtCompound();
        data.setCompound("stopwatches", stopwatches);
        NbtCompound nbt = new NbtCompound();
        nbt.setCompound("data", data);
        nbt.setInt("DataVersion", 1);

        StopWatches result = StopWatches.fromNbt(nbt);

        assertEquals(Long.valueOf(100L), result.getStopwatches().get("minecraft:example"));
    }

    @Test
    void testLoadEmptyData() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 1);
        StopWatches result = StopWatches.fromNbt(nbt);
        assertTrue(result.getStopwatches().isEmpty());
    }

    @Test
    void testSaveAndLoadStopwatches() {
        StopWatches watches = new StopWatches();
        watches.getStopwatches().put("minecraft:example", 100L);

        NbtCompound nbt = watches.toNbt();
        NbtCompound data = nbt.getCompound("data");

        assertTrue(data.has("stopwatches"));
        assertEquals(100L, data.getCompound("stopwatches").getLong("minecraft:example"));

        StopWatches reparsed = StopWatches.fromNbt(nbt);
        assertEquals(Long.valueOf(100L), reparsed.getStopwatches().get("minecraft:example"));
    }

    @Test
    void testSaveLargeNumbers() {
        StopWatches watches = new StopWatches();
        watches.getStopwatches().put("minecraft:example", Long.MAX_VALUE);

        StopWatches reparsed = StopWatches.fromNbt(watches.toNbt());

        assertEquals(Long.valueOf(Long.MAX_VALUE), reparsed.getStopwatches().get("minecraft:example"));
    }
}
