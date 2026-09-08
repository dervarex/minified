package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreboardTest {

    @Test
    void testSaveAndLoadObjective() {
        Scoreboard.Objective objective = new Scoreboard.Objective();
        objective.setName("kills");
        objective.setDisplayName("Kills");
        objective.setCriteriaName("dummy");
        objective.setRenderType("integer");

        Scoreboard.Objective parsed = Scoreboard.Objective.fromNbt(objective.toNbt());

        assertEquals(objective.getName(), parsed.getName());
        assertEquals(objective.getDisplayName(), parsed.getDisplayName());
        assertEquals(objective.getCriteriaName(), parsed.getCriteriaName());
        assertEquals(objective.getRenderType(), parsed.getRenderType());
    }

    @Test
    void testLoadObjectiveWithoutRenderType() {
        NbtCompound nbt = new NbtCompound();
        nbt.setString("Name", "kills");
        nbt.setString("DisplayName", "Kills");
        nbt.setString("CriteriaName", "dummy");

        Scoreboard.Objective objective = Scoreboard.Objective.fromNbt(nbt);

        assertNull(objective.getRenderType());
        assertFalse(objective.toNbt().has("RenderType"));
    }

    @Test
    void testSaveAndLoadPlayerScore() {
        Scoreboard.PlayerScore score = new Scoreboard.PlayerScore();
        score.setName("Steve");
        score.setObjective("kills");
        score.setScore(10);
        score.setLocked(true);

        Scoreboard.PlayerScore parsed = Scoreboard.PlayerScore.fromNbt(score.toNbt());

        assertEquals(score.getName(), parsed.getName());
        assertEquals(score.getObjective(), parsed.getObjective());
        assertEquals(score.getScore(), parsed.getScore());
        assertEquals(score.isLocked(), parsed.isLocked());
    }

    @Test
    void testAddAndRemoveObjectiveAndScore() {
        Scoreboard scoreboard = new Scoreboard();
        Scoreboard.Objective objective = new Scoreboard.Objective();
        Scoreboard.PlayerScore score = new Scoreboard.PlayerScore();

        scoreboard.addObjective(objective);
        scoreboard.addPlayerScore(score);
        assertEquals(1, scoreboard.getObjectives().size());
        assertEquals(1, scoreboard.getPlayerScores().size());

        scoreboard.removeObjective(objective);
        scoreboard.removePlayerScore(score);
        assertTrue(scoreboard.getObjectives().isEmpty());
        assertTrue(scoreboard.getPlayerScores().isEmpty());
    }

    @Test
    void testAddAndRemoveDisplaySlot() {
        Scoreboard scoreboard = new Scoreboard();
        scoreboard.putDisplaySlot("sidebar", "kills");
        assertEquals("kills", scoreboard.getDisplaySlots().get("sidebar"));

        scoreboard.removeDisplaySlot("sidebar");
        assertTrue(scoreboard.getDisplaySlots().isEmpty());
    }

    @Test
    void testLoadDisplaySlots() {
        NbtCompound slots = new NbtCompound();
        slots.setString("sidebar", "kills");
        NbtCompound data = new NbtCompound();
        data.setCompound("DisplaySlots", slots);
        NbtCompound nbt = new NbtCompound();
        nbt.setCompound("data", data);
        nbt.setInt("DataVersion", 1);

        Scoreboard scoreboard = Scoreboard.fromNbt(nbt);

        assertEquals("kills", scoreboard.getDisplaySlots().get("sidebar"));
    }

    @Test
    void testSaveAndLoadScoreboard() {
        Scoreboard original = new Scoreboard();
        original.setDataVersion(3953);
        Scoreboard.Objective objective = new Scoreboard.Objective();
        objective.setName("kills");
        objective.setDisplayName("Kills");
        objective.setCriteriaName("dummy");
        original.addObjective(objective);

        Scoreboard.PlayerScore score = new Scoreboard.PlayerScore();
        score.setName("Steve");
        score.setObjective("kills");
        score.setScore(5);
        original.addPlayerScore(score);

        Scoreboard parsed = Scoreboard.fromNbt(original.toNbt());

        assertEquals(1, parsed.getObjectives().size());
        assertEquals("kills", parsed.getObjectives().get(0).getName());
        assertEquals(1, parsed.getPlayerScores().size());
        assertEquals("Steve", parsed.getPlayerScores().get(0).getName());
        assertEquals(5, parsed.getPlayerScores().get(0).getScore());
    }
}
