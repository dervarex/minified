package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RawJsonKeptTest {
    @Test
    void advancementsKeepUnknownKeys() {
        JsonObject json = JsonParser.parse("""
                {"minecraft:story/root": {"criteria": {"crafting_table": "2026-01-01 10:00:00 +0000"}, "done": true, "mod_field": 1},
                 "mod_key": "kept", "DataVersion": 4000}
                """).asObject();

        Advancements advancements = Advancements.fromJson(json);
        advancements.getAdvancements().get("minecraft:story/root").setDone(false);
        JsonObject written = advancements.toJson();

        assertEquals("kept", written.getString("mod_key"));
        JsonObject root = written.getObject("minecraft:story/root");
        assertFalse(root.getBoolean("done"));
        assertEquals(1, root.getInt("mod_field"));
    }

    @Test
    void removedAdvancementIsGone() {
        JsonObject json = JsonParser.parse("""
                {"a": {"criteria": {}, "done": true}, "b": {"criteria": {}, "done": true}, "DataVersion": 4000}
                """).asObject();

        Advancements advancements = Advancements.fromJson(json);
        advancements.getAdvancements().remove("a");
        JsonObject written = advancements.toJson();

        assertFalse(written.has("a"));
        assertTrue(written.has("b"));
    }

    @Test
    void statsKeepUnknownKeys() {
        JsonObject json = JsonParser.parse("""
                {"stats": {"minecraft:mined": {"minecraft:stone": 5}}, "mod_key": "kept", "DataVersion": 4000}
                """).asObject();

        Stats stats = Stats.fromJson(json);
        stats.getCategories().get("minecraft:mined").put("minecraft:stone", 6);
        JsonObject written = stats.toJson();

        assertEquals("kept", written.getString("mod_key"));
        assertEquals(6, written.getObject("stats").getObject("minecraft:mined").getInt("minecraft:stone"));
    }
}
