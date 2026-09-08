package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.utils.nbt.Parser;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.worlds.save.WorldSave;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

// ./gradlew minified-worlds:manualTest -Dworld.folder="path/to/world"
@Tag("manual")
public class ManualWorldParsingTest {

    private final WorldDataRegistry registry = new WorldDataRegistry();

    @Test
    void parseWorld() {
        String folderPath = System.getProperty("world.folder");

        assumeTrue(folderPath != null && !folderPath.isBlank(),
                "Please provide a path using -Dworld.folder=/path/to/folder");

        Path worldDirectory = Path.of(folderPath);
        assumeTrue(Files.exists(worldDirectory) && Files.isDirectory(worldDirectory),
                "Folder does not exist: " + folderPath);

        System.out.println("Parsing test running with folder: " + worldDirectory.toAbsolutePath());
        System.out.println();

        try {
            Path levelDatPath = worldDirectory.resolve("level.dat");
            NbtCompound root = Parser.readFile(levelDatPath.toFile());
            NbtCompound levelData = root.getCompound("Data");

            WorldSave worldSave = new WorldSave(worldDirectory);

            registerAll();
            registry.printAll(worldSave);

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read level.dat", e);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void registerAll() {
        registry.register("CustomBossEvents", WorldSave::getCustomBossEvents, DataPrinters.PRINT_BOSS_EVENTS);
        registry.register("GameRules", WorldSave::getGameRules, DataPrinters.PRINT_GAME_RULES);
        registry.register("Level", ws -> Optional.ofNullable(ws.getLevel()), DataPrinters.PRINT_LEVEL);
        registry.register("SessionLock", ws -> Optional.ofNullable(ws.getLock()), DataPrinters.PRINT_SESSION_LOCK);
        registry.register("RandomSequences", WorldSave::getRandomSequences, DataPrinters.PRINT_RANDOM_SEQUENCES);
        registry.register("Scoreboard", WorldSave::getScoreboard, DataPrinters.PRINT_SCOREBOARD);
        registry.register("StopWatches", WorldSave::getStopWatches, DataPrinters.PRINT_STOPWATCHES);
        registry.register("WanderingTrader", WorldSave::getWanderingTrader, DataPrinters.PRINT_WANDERING_TRADER);
        registry.register("Weather", WorldSave::getWeather, DataPrinters.PRINT_WEATHER);
        registry.register("WorldClocks", WorldSave::getWorldClocks, DataPrinters.PRINT_WORLD_CLOCKS);
        registry.register("WorldGenSettings", WorldSave::getWorldGenSettings, DataPrinters.PRINT_WORLD_GEN_SETTINGS);
        registry.register("Players", ws -> Optional.of(ws.getPlayers()).filter(players -> !players.isEmpty()), DataPrinters.PRINT_PLAYERS);
    }
}