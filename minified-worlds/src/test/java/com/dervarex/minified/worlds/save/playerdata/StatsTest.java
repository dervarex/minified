package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsTest {

    @Test
    void testLoadEmptyData() {
        Stats stats = Stats.fromJson(new JsonObject());
        assertEquals(0, stats.getDataVersion());
        assertTrue(stats.getCategories().isEmpty());
    }

    @Test
    void testLoadUnknownStatReturnsZero() {
        Stats stats = new Stats();
        assertEquals(0, stats.getStat("minecraft:custom", "minecraft:jump"));
    }

    @Test
    void testSaveAndLoadStats() {
        Stats original = new Stats();
        original.setDataVersion(3953);
        original.getCategories().put("minecraft:custom", new java.util.LinkedHashMap<>(
                java.util.Map.of("minecraft:jump", 42)));

        Stats parsed = Stats.fromJson(original.toJson());

        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(42, parsed.getStat("minecraft:custom", "minecraft:jump"));
    }
}