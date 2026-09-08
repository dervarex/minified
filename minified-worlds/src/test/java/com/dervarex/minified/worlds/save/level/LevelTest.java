package com.dervarex.minified.worlds.save.level;

import com.dervarex.minified.utils.nbt.NbtEquals;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LevelTest {

    private static Level sample() {
        Level level = new Level();
        level.setDifficulty(Difficulty.normal);
        level.setHardcore(false);
        level.setLocked(false);
        level.setEnabledDatapacks(new String[]{"vanilla", "bundle"});
        level.setDisabledDatapacks(new String[]{});
        level.setSpawn(new Spawn());
        level.setVersion(new Version());
        level.setEnabledFeatures(new String[]{"minecraft:vanilla"});
        level.setServerBrands(new String[]{"vanilla"});
        level.setAllowCommands(true);
        level.setDataVersion(3953);
        level.setGameType(0);
        level.setInitialized((byte) 1);
        level.setLastPlayed(1234567890L);
        level.setLevelName("Test World");
        level.setTime(42);
        level.setNbtVersion(19133);
        level.setModded(false);
        level.setSingleplayerUuid(UUID.fromString("a31ccf30-00e4-4928-a590-e366c90af710"));
        return level;
    }

    @Test
    void testSaveAndLoadLevel() {
        Level original = sample();
        NbtCompound nbt = original.toNbt();
        Level parsed = Level.fromNbt(nbt);

        assertEquals(original.getDifficulty(), parsed.getDifficulty());
        assertEquals(original.isHardcore(), parsed.isHardcore());
        assertEquals(original.isLocked(), parsed.isLocked());
        assertArrayEquals(original.getEnabledDatapacks(), parsed.getEnabledDatapacks());
        assertArrayEquals(original.getDisabledDatapacks(), parsed.getDisabledDatapacks());
        assertArrayEquals(original.getEnabledFeatures(), parsed.getEnabledFeatures());
        assertArrayEquals(original.getServerBrands(), parsed.getServerBrands());
        assertEquals(original.isAllowCommands(), parsed.isAllowCommands());
        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(original.getGameType(), parsed.getGameType());
        assertEquals(original.getInitialized(), parsed.getInitialized());
        assertEquals(original.getLastPlayed(), parsed.getLastPlayed());
        assertEquals(original.getLevelName(), parsed.getLevelName());
        assertEquals(original.getTime(), parsed.getTime());
        assertEquals(original.getNbtVersion(), parsed.getNbtVersion());
        assertEquals(original.isModded(), parsed.isModded());
        assertEquals(original.getSingleplayerUuid(), parsed.getSingleplayerUuid());
    }

    @Test
    void testSaveTwiceMatches() {
        Level level = sample();
        NbtCompound first = level.toNbt();
        NbtCompound second = Level.fromNbt(first).toNbt();
        assertTrue(NbtEquals.deepEquals(first, second));
    }

    @Test
    void testSaveAndLoadEmptyDatapacks() {
        Level level = sample();
        level.setEnabledDatapacks(new String[0]);
        level.setDisabledDatapacks(new String[0]);

        Level parsed = Level.fromNbt(level.toNbt());
        assertEquals(0, parsed.getEnabledDatapacks().length);
        assertEquals(0, parsed.getDisabledDatapacks().length);
    }
}
