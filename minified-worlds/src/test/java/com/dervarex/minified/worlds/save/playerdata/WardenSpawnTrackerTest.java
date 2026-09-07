package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WardenSpawnTrackerTest {

    @Test
    void testLoadEmptyData() {
        WardenSpawnTracker tracker = WardenSpawnTracker.fromNbt(new NbtCompound());
        assertNull(tracker.getCooldownTicks());
        assertNull(tracker.getTicksSinceLastWarning());
        assertNull(tracker.getWarningLevel());
    }

    @Test
    void testSaveWithoutOptionalFields() {
        WardenSpawnTracker tracker = new WardenSpawnTracker();
        tracker.setWarningLevel(2);

        NbtCompound nbt = tracker.toNbt();

        assertFalse(nbt.has("cooldown_ticks"));
        assertFalse(nbt.has("ticks_since_last_warning"));
        assertEquals(2, nbt.getInt("warning_level"));
    }

    @Test
    void testSaveAndLoadWardenSpawnTracker() {
        WardenSpawnTracker original = new WardenSpawnTracker();
        original.setCooldownTicks(100);
        original.setTicksSinceLastWarning(50);
        original.setWarningLevel(1);

        WardenSpawnTracker parsed = WardenSpawnTracker.fromNbt(original.toNbt());

        assertEquals(original.getCooldownTicks(), parsed.getCooldownTicks());
        assertEquals(original.getTicksSinceLastWarning(), parsed.getTicksSinceLastWarning());
        assertEquals(original.getWarningLevel(), parsed.getWarningLevel());
    }
}