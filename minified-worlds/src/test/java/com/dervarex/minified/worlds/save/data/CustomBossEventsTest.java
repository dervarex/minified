package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.NbtEquals;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomBossEventsTest {

    @Test
    void testLoadEmptyData() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 3953);

        CustomBossEvents result = CustomBossEvents.fromNbt(nbt);

        assertEquals(3953, result.getDataVersion());
        assertTrue(result.getEvents().isEmpty());
    }

    @Test
    void testAddAndRemoveEvent() {
        CustomBossEvents events = new CustomBossEvents();
        BossEvent event = new BossEvent();
        event.setName("minecraft:wither");

        events.putEvent("minecraft:boss_1", event);
        assertEquals(1, events.getEvents().size());
        assertSame(event, events.getEvents().get("minecraft:boss_1"));

        events.removeEvent("minecraft:boss_1");
        assertTrue(events.getEvents().isEmpty());
    }

    @Test
    void testSaveAndLoadCustomBossEvents() {
        CustomBossEvents original = new CustomBossEvents();
        original.setDataVersion(3953);
        BossEvent event = new BossEvent();
        event.setName("minecraft:ender_dragon");
        event.setVisible(true);
        original.putEvent("minecraft:dragon_fight", event);

        CustomBossEvents parsed = CustomBossEvents.fromNbt(original.toNbt());

        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(1, parsed.getEvents().size());
        BossEvent parsedEvent = parsed.getEvents().get("minecraft:dragon_fight");
        assertEquals("minecraft:ender_dragon", parsedEvent.getName());
        assertTrue(parsedEvent.isVisible());
    }

    @Test
    void testSaveTwiceMatches() {
        CustomBossEvents original = new CustomBossEvents();
        original.setDataVersion(1);
        BossEvent event = new BossEvent();
        event.setName("minecraft:test");
        original.putEvent("id", event);

        NbtCompound first = original.toNbt();
        NbtCompound second = CustomBossEvents.fromNbt(first).toNbt();
        assertTrue(NbtEquals.deepEquals(first, second));
    }
}
