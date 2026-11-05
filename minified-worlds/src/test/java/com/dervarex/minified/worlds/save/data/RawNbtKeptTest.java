package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// changing something must not throw away the fields we don't model
class RawNbtKeptTest {

    private static NbtCompound root(NbtCompound data) {
        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", 4000);
        return root;
    }

    @Test
    void scoreboardKeepsTeamsAndUnknownObjectiveFields() {
        NbtCompound objective = new NbtCompound();

        objective.setString("Name", "kills");
        objective.setString("DisplayName", "Kills");
        objective.setString("CriteriaName", "dummy");
        objective.setBoolean("display_auto_update", true);

        NbtList objectives = new NbtList((byte) 10);
        objectives.add(objective);

        NbtCompound team = new NbtCompound();
        team.setString("Name", "red");
        NbtList teams = new NbtList((byte) 10);
        teams.add(team);

        NbtCompound data = new NbtCompound();
        data.setList("Objectives", objectives);
        data.setList("Teams", teams);

        Scoreboard scoreboard = Scoreboard.fromNbt(root(data));
        scoreboard.getObjectives().get(0).setDisplayName("Kills");
        NbtCompound written = scoreboard.toNbt().getCompound("data");

        assertEquals(1, written.getList("Teams").size());
        NbtCompound writtenObjective = (NbtCompound) written.getList("Objectives").elements().get(0);
        assertEquals("Kills", writtenObjective.getString("DisplayName"));
        assertTrue(writtenObjective.has("display_auto_update"));
    }

    @Test
    void bossEventKeepsUnknownFields() {
        NbtCompound event = new NbtCompound();
        event.setString("Name", "boss");
        event.setString("Color", "red");
        event.setInt("Max", 100);
        NbtCompound data = new NbtCompound();
        data.setCompound("minecraft:boss", event);

        CustomBossEvents events = CustomBossEvents.fromNbt(root(data));
        events.getEvents().get("minecraft:boss").setVisible(true);
        NbtCompound written = events.toNbt().getCompound("data").getCompound("minecraft:boss");

        assertEquals("red", written.getString("Color"));
        assertEquals(100, written.getInt("Max"));
        assertEquals(1, written.getByte("Visible"));
    }

    @Test
    void removedEntriesAreGone() {
        NbtCompound data = new NbtCompound();
        data.setInt("spawn_radius", 10);
        data.setByte("keep_inventory", (byte) 1);

        GameRules rules = GameRules.fromNbt(root(data));
        rules.removeIntegerGamerule("spawn_radius");
        NbtCompound written = rules.toNbt().getCompound("data");

        assertFalse(written.has("spawn_radius"));
        assertTrue(written.has("keep_inventory"));
    }

    @Test
    void worldGenSettingsKeepsUnknownGeneratorFields() {
        NbtCompound generator = new NbtCompound();
        generator.setString("type", "minecraft:noise");
        generator.setString("settings", "minecraft:overworld");
        generator.setLong("custom", 7L);
        NbtCompound dimension = new NbtCompound();
        dimension.setString("type", "minecraft:overworld");
        dimension.setCompound("generator", generator);
        NbtCompound dimensions = new NbtCompound();
        dimensions.setCompound("minecraft:overworld", dimension);
        NbtCompound data = new NbtCompound();
        data.setLong("seed", 1L);
        data.setCompound("dimensions", dimensions);
        data.setString("legacy_custom_options", "kept");

        WorldGenSettings settings = WorldGenSettings.fromNbt(root(data));
        settings.setSeed(2L);
        NbtCompound written = settings.toNbt().getCompound("data");

        assertEquals(2L, written.getLong("seed"));
        assertEquals("kept", written.getString("legacy_custom_options"));
        assertEquals(7L, written.getCompound("dimensions").getCompound("minecraft:overworld")
                .getCompound("generator").getLong("custom"));
    }

    @Test
    void weatherRemovesFieldsSetToNull() {
        NbtCompound data = new NbtCompound();
        data.setInt("rain_time", 50);
        data.setInt("future_field", 1);

        Weather weather = Weather.fromNbt(root(data));
        weather.setRainTime(null);
        NbtCompound written = weather.toNbt().getCompound("data");

        assertFalse(written.has("rain_time"));
        assertTrue(written.has("future_field"));
    }

    @Test
    void textComponentNamesStay() {
        NbtCompound text = new NbtCompound();
        text.setString("text", "Boss");
        text.setString("color", "red");
        NbtCompound event = new NbtCompound();
        event.setCompound("Name", text);

        BossEvent read = BossEvent.fromNbt(event);
        assertNull(read.getName());
        assertEquals("red", read.toNbt().getCompound("Name").getString("color"));

        read.setName("Plain");
        assertEquals("Plain", read.toNbt().getString("Name"));
    }

    @Test
    void objectiveWithComponentDisplayNameReads() {
        NbtCompound objective = new NbtCompound();
        objective.setString("Name", "kills");
        objective.setCompound("DisplayName", new NbtCompound());
        objective.setString("CriteriaName", "dummy");

        Scoreboard.Objective read = Scoreboard.Objective.fromNbt(objective);

        assertNull(read.getDisplayName());
        assertTrue(read.toNbt().get("DisplayName").orElseThrow() instanceof NbtCompound);
    }

    @Test
    void inlineGeneratorSettingsStay() {
        NbtCompound settings = new NbtCompound();
        settings.setInt("sea_level", 40);
        NbtCompound generator = new NbtCompound();
        generator.setString("type", "minecraft:noise");
        generator.setCompound("settings", settings);

        WorldGenSettings.Generator read = WorldGenSettings.Generator.fromNbt(generator);

        assertNull(read.getSettings());
        assertEquals(40, read.toNbt().getCompound("settings").getInt("sea_level"));
    }

    @Test
    void noDataVersionWritesNone() {
        assertFalse(new Weather().toNbt().has("DataVersion"));
        assertFalse(new WanderingTrader().toNbt().has("DataVersion"));
        assertNull(Weather.fromNbt(new NbtCompound()).getDataVersion());
    }
}
