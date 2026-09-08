package com.dervarex.minified.worlds.save;

import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.nbt.Parser;
import com.dervarex.minified.utils.nbt.Writer;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.worlds.save.data.CustomBossEvents;
import com.dervarex.minified.worlds.save.data.GameRules;
import com.dervarex.minified.worlds.save.data.RandomSequences;
import com.dervarex.minified.worlds.save.data.Scoreboard;
import com.dervarex.minified.worlds.save.data.StopWatches;
import com.dervarex.minified.worlds.save.data.WanderingTrader;
import com.dervarex.minified.worlds.save.data.Weather;
import com.dervarex.minified.worlds.save.data.WorldClocks;
import com.dervarex.minified.worlds.save.data.WorldGenSettings;
import com.dervarex.minified.worlds.save.level.Level;
import com.dervarex.minified.worlds.save.playerdata.Player;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a world.
 * Note: Fields are marked nullable to prevent NullPointerExceptions,
 * although Minecraft usually generates default Nbt Structures when loading a World.
 */
public class WorldSave {
    @Getter
    private final Path worldDirectory;
    @Getter
    private final Level level;
    @Nullable
    private final CustomBossEvents customBossEvents;
    @Nullable
    private final GameRules gameRules;
    @Nullable
    private final RandomSequences randomSequences;
    @Nullable
    private final Scoreboard scoreboard;
    @Nullable
    private final StopWatches stopWatches;
    @Nullable
    private final WanderingTrader wanderingTrader;
    @Nullable
    private final Weather weather;
    @Nullable
    private final WorldClocks worldClocks;
    @Nullable
    private final WorldGenSettings worldGenSettings;
    @Getter
    private final List<Player> players;
    @Getter
    private final SessionLock lock;

    public WorldSave(Path worldDirectory) {
        this.worldDirectory = worldDirectory;
        Path dataDirectory = worldDirectory.resolve("data").resolve("minecraft");

        if (!Files.isDirectory(worldDirectory)) {
            throw new RuntimeException("World directory does not exist or is not a directory: " + worldDirectory);
        }

        Path levelDatPath = worldDirectory.resolve("level.dat");
        Path customBossEventsPath = dataDirectory.resolve("custom_boss_events.dat");
        Path gameRulesPath = dataDirectory.resolve("game_rules.dat");
        Path randomSequencesPath = dataDirectory.resolve("random_sequences.dat");
        Path scoreboardPath = dataDirectory.resolve("scoreboard.dat");
        Path stopWatchesPath = dataDirectory.resolve("stopwatches.dat");
        Path wanderingTraderPath = dataDirectory.resolve("wandering_trader.dat");
        Path weatherPath = dataDirectory.resolve("weather.dat");
        Path worldClocksPath = dataDirectory.resolve("world_clocks.dat");
        Path worldGenSettingsPath = dataDirectory.resolve("world_gen_settings.dat");

        if (!Files.exists(levelDatPath)) {
            throw new RuntimeException("World directory does not contain level.dat file: " + worldDirectory);
        }

        try {
            this.level = Level.fromNbt(Parser.readFile(levelDatPath.toFile()));

            this.customBossEvents = Files.exists(customBossEventsPath)
                    ? CustomBossEvents.fromNbt(Parser.readFile(customBossEventsPath.toFile()))
                    : null;
            this.gameRules = Files.exists(gameRulesPath)
                    ? GameRules.fromNbt(Parser.readFile(gameRulesPath.toFile()))
                    : null;
            this.randomSequences = Files.exists(randomSequencesPath)
                    ? RandomSequences.fromNbt(Parser.readFile(randomSequencesPath.toFile()))
                    : null;
            this.scoreboard = Files.exists(scoreboardPath)
                    ? Scoreboard.fromNbt(Parser.readFile(scoreboardPath.toFile()))
                    : null;
            this.stopWatches = Files.exists(stopWatchesPath)
                    ? StopWatches.fromNbt(Parser.readFile(stopWatchesPath.toFile()))
                    : null;
            this.wanderingTrader = Files.exists(wanderingTraderPath)
                    ? WanderingTrader.fromNbt(Parser.readFile(wanderingTraderPath.toFile()))
                    : null;
            this.weather = Files.exists(weatherPath)
                    ? Weather.fromNbt(Parser.readFile(weatherPath.toFile()))
                    : null;
            this.worldClocks = Files.exists(worldClocksPath)
                    ? WorldClocks.fromNbt(Parser.readFile(worldClocksPath.toFile()))
                    : null;
            this.worldGenSettings = Files.exists(worldGenSettingsPath)
                    ? WorldGenSettings.fromNbt(Parser.readFile(worldGenSettingsPath.toFile()))
                    : null;
            this.players = readPlayers(worldDirectory);
            this.lock = new SessionLock(worldDirectory.resolve("session.lock"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Player> readPlayers(Path worldDirectory) throws IOException {
        Path playersDirectory = worldDirectory.resolve("players");
        Path playerDataDirectory = playersDirectory.resolve("data");
        if (!Files.isDirectory(playerDataDirectory)) {
            return List.of();
        }

        Path advancementsDirectory = playersDirectory.resolve("advancements");
        Path statsDirectory = playersDirectory.resolve("stats");

        List<Player> result = new ArrayList<>();
        List<Path> playerFiles;
        try (var stream = Files.list(playerDataDirectory)) {
            playerFiles = stream
                    .filter(path -> path.toString().endsWith(".dat"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }

        for (Path playerFile : playerFiles) {
            String uuid = playerFile.getFileName().toString().replace(".dat", "");
            NbtCompound nbt = Parser.readFile(playerFile.toFile());

            Path advancementsFile = advancementsDirectory.resolve(uuid + ".json");
            Path statsFile = statsDirectory.resolve(uuid + ".json");

            var advancements = Files.exists(advancementsFile)
                    ? new JsonFile(advancementsFile).asObject()
                    : null;
            var stats = Files.exists(statsFile)
                    ? new JsonFile(statsFile).asObject()
                    : null;

            result.add(Player.fromNbt(nbt, advancements, stats));
        }

        return result;
    }

    public Optional<CustomBossEvents> getCustomBossEvents() {
        return Optional.ofNullable(customBossEvents);
    }
    public Optional<GameRules> getGameRules() {
        return Optional.ofNullable(gameRules);
    }
    public Optional<RandomSequences> getRandomSequences() {
        return Optional.ofNullable(randomSequences);
    }
    public Optional<Scoreboard> getScoreboard() {
        return Optional.ofNullable(scoreboard);
    }
    public Optional<StopWatches> getStopWatches() {
        return Optional.ofNullable(stopWatches);
    }
    public Optional<WanderingTrader> getWanderingTrader() {
        return Optional.ofNullable(wanderingTrader);
    }
    public Optional<Weather> getWeather() {
        return Optional.ofNullable(weather);
    }
    public Optional<WorldClocks> getWorldClocks() {
        return Optional.ofNullable(worldClocks);
    }
    public Optional<WorldGenSettings> getWorldGenSettings() {
        return Optional.ofNullable(worldGenSettings);
    }

    public void save() throws IOException {
        NbtCompound nbt = level.toNbt();
        Writer.writeFile(worldDirectory.resolve("level.dat").toFile(), nbt);
    }
}
