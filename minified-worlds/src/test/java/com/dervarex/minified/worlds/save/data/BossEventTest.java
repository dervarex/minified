package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossEventTest {

    @Test
    void testLoadEmptyData() {
        BossEvent event = BossEvent.fromNbt(new NbtCompound());
        assertNull(event.getName());
        assertFalse(event.isVisible());
    }

    @Test
    void testSaveWithoutName() {
        BossEvent event = new BossEvent();
        event.setVisible(true);

        NbtCompound nbt = event.toNbt();

        assertFalse(nbt.has("Name"));
        assertEquals((byte) 1, nbt.getByte("Visible"));
    }

    @Test
    void testSaveAndLoadBossEvent() {
        BossEvent original = new BossEvent();
        original.setName("minecraft:ender_dragon");
        original.setVisible(true);

        BossEvent parsed = BossEvent.fromNbt(original.toNbt());

        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.isVisible(), parsed.isVisible());
    }
}
