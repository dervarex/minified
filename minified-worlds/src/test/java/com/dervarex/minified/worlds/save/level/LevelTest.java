package com.dervarex.minified.worlds.save.level;

import com.dervarex.minified.utils.nbt.NbtEquals;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtInt;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
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

    private static NbtList strings(String... values) {
        NbtList list = new NbtList((byte) 8);
        for (String value : values) list.add(new NbtString(value));
        return list;
    }

    // every key 26.3 writes into level.dat, plus one from a mod
    private static NbtCompound levelDat() {
        NbtCompound difficultySettings = new NbtCompound();
        difficultySettings.setString("difficulty", "normal");
        difficultySettings.setByte("hardcore", (byte) 1);
        difficultySettings.setByte("locked", (byte) 0);
        NbtCompound spawn = new NbtCompound();
        spawn.setIntArray("pos", new int[]{0, 64, 0});
        spawn.setFloat("pitch", 0.5f);
        spawn.setString("dimension", "minecraft:overworld");
        spawn.setFloat("yaw", 90.25f);
        NbtCompound version = new NbtCompound();
        version.setByte("Snapshot", (byte) 0);
        version.setString("Series", "main");
        version.setInt("Id", 5023);
        version.setString("Name", "26.3");
        NbtList history = new NbtList((byte) 3);
        history.add(new NbtInt(5023));
        NbtCompound dataPacks = new NbtCompound();
        dataPacks.setList("Enabled", strings("vanilla"));
        dataPacks.setList("Disabled", strings("minecraft_improvements", "redstone_experiments", "trade_rebalance"));

        NbtCompound data = new NbtCompound();
        data.setCompound("difficulty_settings", difficultySettings);
        data.setIntArray("singleplayer_uuid", new int[]{1, 2, 3, 4});
        data.setLong("Time", 228);
        data.setList("version_history", history);
        data.setInt("GameType", 0);
        data.setList("ServerBrands", strings("fabric"));
        data.setInt("version", 19133);
        data.setLong("LastPlayed", 1791662134259L);
        data.setCompound("spawn", spawn);
        data.setCompound("Version", version);
        data.setString("LevelName", "New World");
        data.setByte("initialized", (byte) 1);
        data.setByte("WasModded", (byte) 1);
        data.setInt("DataVersion", 5023);
        data.setByte("allowCommands", (byte) 0);
        data.setCompound("DataPacks", dataPacks);
        data.setString("some_mod:extra", "keep me");
        NbtCompound root = new NbtCompound();
        root.setCompound("Data", data);
        return root;
    }

    @Test
    void writesBackExactlyWhatItRead() {
        NbtCompound original = levelDat();

        assertTrue(NbtEquals.deepEquals(original, Level.fromNbt(original).toNbt()));
    }

    @Test
    void editsKeepWhatMinifiedDoesntKnow() {
        Level level = Level.fromNbt(levelDat());
        level.setLevelName("Renamed");
        level.getSpawn().setYaw(-45.5f);

        NbtCompound data = level.toNbt().getCompound("Data");

        assertEquals("Renamed", data.getString("LevelName"));
        assertEquals("keep me", data.getString("some_mod:extra"));
        assertEquals(1, data.getList("version_history").size());
        assertEquals(-45.5f, data.getCompound("spawn").getFloat("yaw"));
        assertEquals(0.5f, data.getCompound("spawn").getFloat("pitch"));
        assertTrue(level.isHardcore());
    }

    @Test
    void missingFieldsStayMissing() {
        NbtCompound original = levelDat();
        NbtCompound data = original.getCompound("Data");
        data.remove("singleplayer_uuid");
        data.remove("DataPacks");
        data.remove("spawn");

        Level level = Level.fromNbt(original);
        assertNull(level.getEnabledFeatures()); // 26.3 only writes it with experiments turned on
        assertNull(level.getSingleplayerUuid());
        assertNull(level.getEnabledDatapacks());
        assertNull(level.getSpawn());

        assertTrue(NbtEquals.deepEquals(original, level.toNbt()));
    }

    @Test
    void clearedFieldsAreRemoved() {
        Level level = Level.fromNbt(levelDat());
        level.setSingleplayerUuid(null);
        level.setServerBrands(null);

        NbtCompound data = level.toNbt().getCompound("Data");

        assertFalse(data.has("singleplayer_uuid"));
        assertFalse(data.has("ServerBrands"));
    }
}
