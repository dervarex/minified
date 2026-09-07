package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdvancementsTest {

    @Test
    void testLoadEmptyData() {
        Advancements result = Advancements.fromJson(new JsonObject());
        assertEquals(0, result.getDataVersion());
        assertTrue(result.getAdvancements().isEmpty());
    }

    @Test
    void testLoadSkipsInvalidData() {
        JsonObject json = new JsonObject();
        json.put("DataVersion", 3953);
        json.put("minecraft:story/root", new JsonObject());
        json.put("stray_string", "value");

        Advancements result = Advancements.fromJson(json);

        assertEquals(3953, result.getDataVersion());
        assertEquals(1, result.getAdvancements().size());
        assertTrue(result.getAdvancements().containsKey("minecraft:story/root"));
    }

    @Test
    void testSaveAndLoadAdvancement() {
        Advancements.Advancement advancement = new Advancements.Advancement();
        advancement.setDone(true);
        advancement.getCriteria().put("obtained_dirt", "2024-01-01T00:00:00Z");

        Advancements.Advancement parsed = Advancements.Advancement.fromJson(advancement.toJson());

        assertTrue(parsed.isDone());
        assertEquals("2024-01-01T00:00:00Z", parsed.getCriterionDate("obtained_dirt"));
        assertNull(parsed.getCriterionDate("missing"));
    }

    @Test
    void testSaveAndLoadAdvancements() {
        Advancements original = new Advancements();
        original.setDataVersion(3953);
        Advancements.Advancement advancement = new Advancements.Advancement();
        advancement.setDone(true);
        advancement.getCriteria().put("root", "2024-01-01T00:00:00Z");
        original.getAdvancements().put("minecraft:story/root", advancement);

        Advancements parsed = Advancements.fromJson(original.toJson());

        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(1, parsed.getAdvancements().size());
        assertTrue(parsed.getAdvancements().get("minecraft:story/root").isDone());
    }
}