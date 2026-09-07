package com.dervarex.minified.worlds.save;

import com.dervarex.minified.utils.nbt.Parser;
import com.dervarex.minified.utils.nbt.Writer;
import com.dervarex.minified.worlds.save.level.Difficulty;
import com.dervarex.minified.worlds.save.level.Level;
import com.dervarex.minified.worlds.save.level.Spawn;
import com.dervarex.minified.worlds.save.level.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        assertThrows(RuntimeException.class, () -> new WorldSave(missing));
    }

    @Test
    void testInitWithMissingLevelDatFails(@TempDir Path dir) throws IOException {
        Path worldDir = Files.createDirectories(dir.resolve("world"));
        assertThrows(RuntimeException.class, () -> new WorldSave(worldDir));
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
    }
}