package com.dervarex.minified.worlds.save;

import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.nbt.NbtEquals;
import com.dervarex.minified.utils.nbt.Parser;
import com.dervarex.minified.utils.nbt.Writer;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.worlds.exceptions.InvalidWorldException;
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
import com.dervarex.minified.worlds.save.playerdata.Advancements;
import com.dervarex.minified.worlds.save.playerdata.Player;
import com.dervarex.minified.worlds.save.playerdata.Stats;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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

    // what each file looked like the last time we touched it, save() only writes what changed since
    private final List<NbtFile> nbtFiles = new ArrayList<>();
    private final List<JsonFileState> jsonFiles = new ArrayList<>();

    public WorldSave(Path worldDirectory) {
        this.worldDirectory = worldDirectory;
        Path dataDirectory = worldDirectory.resolve("data").resolve("minecraft");

        if (!Files.isDirectory(worldDirectory)) {
            throw new InvalidWorldException("World directory does not exist or is not a directory: " + worldDirectory, worldDirectory);
        }

        Path levelDatPath = worldDirectory.resolve("level.dat");

        if (!Files.exists(levelDatPath)) {
            throw new InvalidWorldException("World directory does not contain level.dat file: " + worldDirectory, worldDirectory);
        }

        try {
            this.level = Level.fromNbt(Parser.readFile(levelDatPath.toFile()));
            track(levelDatPath, worldDirectory.resolve("level.dat_old"), level::toNbt);

            this.customBossEvents = load(dataDirectory.resolve("custom_boss_events.dat"), CustomBossEvents::fromNbt, CustomBossEvents::toNbt);
            this.gameRules = load(dataDirectory.resolve("game_rules.dat"), GameRules::fromNbt, GameRules::toNbt);
            this.randomSequences = load(dataDirectory.resolve("random_sequences.dat"), RandomSequences::fromNbt, RandomSequences::toNbt);
            this.scoreboard = load(dataDirectory.resolve("scoreboard.dat"), Scoreboard::fromNbt, Scoreboard::toNbt);
            this.stopWatches = load(dataDirectory.resolve("stopwatches.dat"), StopWatches::fromNbt, StopWatches::toNbt);
            this.wanderingTrader = load(dataDirectory.resolve("wandering_trader.dat"), WanderingTrader::fromNbt, WanderingTrader::toNbt);
            this.weather = load(dataDirectory.resolve("weather.dat"), Weather::fromNbt, Weather::toNbt);
            this.worldClocks = load(dataDirectory.resolve("world_clocks.dat"), WorldClocks::fromNbt, WorldClocks::toNbt);
            this.worldGenSettings = load(dataDirectory.resolve("world_gen_settings.dat"), WorldGenSettings::fromNbt, WorldGenSettings::toNbt);
            this.players = readPlayers(worldDirectory);
            this.lock = new SessionLock(worldDirectory.resolve("session.lock"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T load(Path file, Function<NbtCompound, T> parse, Function<T, NbtCompound> write) throws IOException {
        if (!Files.exists(file)) return null;
        T value = parse.apply(Parser.readFile(file.toFile()));
        track(file, null, () -> write.apply(value));
        return value;
    }

    private void track(Path file, Path backup, Supplier<NbtCompound> content) {
        nbtFiles.add(new NbtFile(file, backup, content, content.get().copy()));
    }

    private List<Player> readPlayers(Path worldDirectory) throws IOException {
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

            Player player = Player.fromNbt(nbt, advancements, stats);
            result.add(player);

            // <uuid>.dat_old, mojang does that too
            track(playerFile, playerDataDirectory.resolve(uuid + ".dat_old"), player::toNbt);
            trackJson(advancementsFile, () -> Optional.ofNullable(player.getAdvancements()).map(Advancements::toJson).orElse(null));
            trackJson(statsFile, () -> Optional.ofNullable(player.getStatistics()).map(Stats::toJson).orElse(null));
        }

        return result;
    }

    private void trackJson(Path file, Supplier<JsonObject> content) {
        JsonObject json = content.get();
        jsonFiles.add(new JsonFileState(file, content, json == null ? null : json.toJson()));
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

    /**
     * Writes whatever changed since loading (or the last save): level.dat, data/minecraft and the players.
     * level.dat and the player files keep a .dat_old, like Minecraft does. Chunks go through
     * {@link com.dervarex.minified.worlds.dimension.Dimension#saveChunkData} instead
     * @throws IOException if writing fails, or Minecraft has the world open right now
     */
    public void save() throws IOException {
        try (SessionLock.Handle handle = SessionLock.acquire(worldDirectory)) {
            for (NbtFile file : nbtFiles) {
                NbtCompound current = file.content.get();
                if (NbtEquals.deepEquals(file.saved, current)) continue;
                if (file.backup != null) {
                    Writer.writeFile(file.path.toFile(), current, file.backup.toFile());
                } else {
                    Writer.writeFile(file.path.toFile(), current);
                }
                file.saved = current.copy();
            }
            for (JsonFileState file : jsonFiles) {
                JsonObject current = file.content.get();
                String json = current == null ? null : current.toJson();
                if (json == null || json.equals(file.saved)) continue;
                writeJson(file.path, json);
                file.saved = json;
            }
        }
    }

    private static void writeJson(Path file, String json) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.writeString(temp, json, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static final class NbtFile {
        private final Path path;
        private final Path backup; // null if mojang doesn't bother with one either
        private final Supplier<NbtCompound> content;
        private NbtCompound saved;

        private NbtFile(Path path, Path backup, Supplier<NbtCompound> content, NbtCompound saved) {
            this.path = path;
            this.backup = backup;
            this.content = content;
            this.saved = saved;
        }
    }

    private static final class JsonFileState {
        private final Path path;
        private final Supplier<JsonObject> content;
        private String saved; // null if there was no file

        private JsonFileState(Path path, Supplier<JsonObject> content, String saved) {
            this.path = path;
            this.content = content;
            this.saved = saved;
        }
    }
}
