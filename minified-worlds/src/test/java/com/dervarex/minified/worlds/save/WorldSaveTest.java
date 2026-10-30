package com.dervarex.minified.worlds.save;

import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.nbt.Parser;
import com.dervarex.minified.utils.nbt.Writer;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.worlds.exceptions.InvalidWorldException;
import com.dervarex.minified.worlds.save.data.GameRules;
import com.dervarex.minified.worlds.save.level.Difficulty;
import com.dervarex.minified.worlds.save.level.Level;
import com.dervarex.minified.worlds.save.level.Spawn;
import com.dervarex.minified.worlds.save.level.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorldSaveTest {

    private static Level minimalLevel() {
        Level level = new Level();
        level.setDifficulty(Difficulty.normal);
        level.setEnabledDatapacks(new String[]{"vanilla"});
        level.setDisabledDatapacks(new String[]{});
        level.setSpawn(new Spawn());
        level.setVersion(new Version());
        level.setEnabledFeatures(new String[]{});
        level.setServerBrands(new String[]{});
        level.setLevelName("Test World");
        level.setSingleplayerUuid(UUID.randomUUID());
        return level;
    }

    private static Path buildWorld(Path dir, Level level) throws IOException {
        Files.createDirectories(dir);
        Writer.writeFile(dir.resolve("level.dat").toFile(), level.toNbt());
        return dir;
    }

    @Test
    void testInitWithMissingDirectoryFails(@TempDir Path dir) {
        Path missing = dir.resolve("no-such-world");
        assertThrows(InvalidWorldException.class, () -> new WorldSave(missing));
    }

    @Test
    void testInitWithMissingLevelDatFails(@TempDir Path dir) throws IOException {
        Path worldDir = Files.createDirectories(dir.resolve("world"));
        assertThrows(InvalidWorldException.class, () -> new WorldSave(worldDir));
    }

    @Test
    void testLoadWorldSave(@TempDir Path dir) throws IOException {
        Level level = minimalLevel();
        Path worldDir = buildWorld(dir.resolve("world"), level);

        WorldSave save = new WorldSave(worldDir);

        assertEquals("Test World", save.getLevel().getLevelName());
        assertEquals(Difficulty.normal, save.getLevel().getDifficulty());
        assertEquals(worldDir, save.getWorldDirectory());
    }

    @Test
    void testLoadWorldSaveWithoutOptionalData(@TempDir Path dir) throws IOException {
        Path worldDir = buildWorld(dir.resolve("world"), minimalLevel());

        WorldSave save = new WorldSave(worldDir);

        assertTrue(save.getCustomBossEvents().isEmpty());
        assertTrue(save.getGameRules().isEmpty());
    }

    @Test
    void testSaveWorldSave(@TempDir Path dir) throws IOException {
        Path worldDir = buildWorld(dir.resolve("world"), minimalLevel());
        WorldSave save = new WorldSave(worldDir);

        save.getLevel().setLevelName("Renamed World");
        save.save();

        Level reloaded = Level.fromNbt(Parser.readFile(worldDir.resolve("level.dat").toFile()));
        assertEquals("Renamed World", reloaded.getLevelName());
        Level backup = Level.fromNbt(Parser.readFile(worldDir.resolve("level.dat_old").toFile()));
        assertEquals("Test World", backup.getLevelName());
    }

    @Test
    void loadsWorldsWithoutExperiments(@TempDir Path dir) throws IOException {
        Level level = minimalLevel();
        level.setEnabledFeatures(null); // 26.3 only writes enabled_features when experiments are turned on
        Path worldDir = buildWorld(dir.resolve("world"), level);

        assertNull(new WorldSave(worldDir).getLevel().getEnabledFeatures());
    }

    @Test
    void saveWritesWhatChangedAndNothingElse(@TempDir Path dir) throws IOException {
        Path worldDir = buildWorld(dir.resolve("world"), minimalLevel());
        Path data = Files.createDirectories(worldDir.resolve("data").resolve("minecraft"));
        GameRules rules = new GameRules();
        rules.setDataVersion(5023);
        rules.putBooleanGamerule("minecraft:keep_inventory", false);
        Writer.writeFile(data.resolve("game_rules.dat").toFile(), rules.toNbt());
        // Scoreboard has no idea what this is, rewriting the file would eat it
        NbtCompound scoreboardData = new NbtCompound();
        scoreboardData.setString("some_mod:extra", "keep me");
        NbtCompound scoreboard = new NbtCompound();
        scoreboard.setCompound("data", scoreboardData);
        scoreboard.setInt("DataVersion", 5023);
        Writer.writeFile(data.resolve("scoreboard.dat").toFile(), scoreboard);

        WorldSave save = new WorldSave(worldDir);
        save.getGameRules().orElseThrow().putBooleanGamerule("minecraft:keep_inventory", true);
        save.save();

        GameRules written = GameRules.fromNbt(Parser.readFile(data.resolve("game_rules.dat").toFile()));
        assertTrue(written.getBooleanGamerules().get("minecraft:keep_inventory"));
        assertEquals("keep me", Parser.readFile(data.resolve("scoreboard.dat").toFile()).getCompound("data").getString("some_mod:extra"));
        assertFalse(Files.exists(worldDir.resolve("level.dat_old")), "level.dat didn't change, so it shouldn't have been written");
    }

    @Test
    void savesPlayersAndKeepsTheirOldFile(@TempDir Path dir) throws IOException {
        Path worldDir = buildWorld(dir.resolve("world"), minimalLevel());
        Path playerData = Files.createDirectories(worldDir.resolve("players").resolve("data"));
        Path playerStats = Files.createDirectories(worldDir.resolve("players").resolve("stats"));
        String uuid = "a31ccf30-00e4-4928-a590-e366c90af710";
        NbtCompound player = new NbtCompound();
        player.setInt("DataVersion", 5023);
        player.setFloat("Health", 20f);
        player.setList("EnderItems", new NbtList((byte) 10));
        Writer.writeFile(playerData.resolve(uuid + ".dat").toFile(), player);
        Files.writeString(playerStats.resolve(uuid + ".json"), "{\"stats\":{\"minecraft:custom\":{\"minecraft:jump\":3}},\"DataVersion\":5023}");

        WorldSave save = new WorldSave(worldDir);
        save.getPlayers().getFirst().setHealth(4f);
        save.getPlayers().getFirst().getStatistics().getCategories().get("minecraft:custom").put("minecraft:jump", 4);
        save.save();

        NbtCompound written = Parser.readFile(playerData.resolve(uuid + ".dat").toFile());
        assertEquals(4f, written.getFloat("Health"));
        assertTrue(written.has("EnderItems"));
        assertEquals(20f, Parser.readFile(playerData.resolve(uuid + ".dat_old").toFile()).getFloat("Health"));
        JsonObject stats = new JsonFile(playerStats.resolve(uuid + ".json")).asObject();
        assertEquals(4, stats.getObject("stats").getObject("minecraft:custom").getInt("minecraft:jump"));
    }

    @Test
    void refusesToSaveWhileTheGameHasTheWorldOpen(@TempDir Path dir) throws IOException {
        Path worldDir = buildWorld(dir.resolve("world"), minimalLevel());
        Path lockFile = worldDir.resolve("session.lock");
        Files.write(lockFile, new byte[0]);
        WorldSave save = new WorldSave(worldDir);
        save.getLevel().setLevelName("Renamed World");

        try (FileChannel game = FileChannel.open(lockFile, StandardOpenOption.WRITE); FileLock held = game.lock()) {
            assertThrows(IOException.class, save::save);
        }
        assertEquals("Test World", Level.fromNbt(Parser.readFile(worldDir.resolve("level.dat").toFile())).getLevelName());
    }
}