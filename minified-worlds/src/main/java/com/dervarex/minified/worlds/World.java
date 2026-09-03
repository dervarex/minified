package com.dervarex.minified.worlds;

import com.dervarex.minified.utils.nbt.Parser;
import com.dervarex.minified.utils.nbt.Writer;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.worlds.world.SessionLock;
import com.dervarex.minified.worlds.data.CustomBossEvents;
import com.dervarex.minified.worlds.data.GameRules;
import com.dervarex.minified.worlds.level.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a world.
 * Note: Fields are marked nullable to prevent NullPointerExceptions,
 * although Minecraft usually generates default Nbt Structures when loading a World.
 */
public class World {
    @Getter
    private final Path worldDirectory;
    @Getter
    private final Level level;
    @Nullable
    private final CustomBossEvents customBossEvents;
    @Nullable
    private final GameRules gameRules;
    @Getter
    private final SessionLock lock;

    public World(Path worldDirectory) {
        this.worldDirectory = worldDirectory;
        Path dataDirectory = worldDirectory.resolve("data");

        if (!Files.isDirectory(worldDirectory)) {
            throw new RuntimeException("World directory does not exist or is not a directory: " + worldDirectory);
        }

        Path levelDatPath = worldDirectory.resolve("level.dat");
        Path customBossEventsPath = dataDirectory.resolve("custom_boss_events.dat");
        Path gameRulesPath = dataDirectory.resolve("game_rules.dat");

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
            this.lock = new SessionLock(worldDirectory.resolve("session.lock"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<CustomBossEvents> getCustomBossEvents() {
        return Optional.ofNullable(customBossEvents);
    }
    public Optional<GameRules> getGameRules() {
        return Optional.ofNullable(gameRules);
    }

    public void save() throws IOException {
        NbtCompound nbt = level.toNbt();
        Writer.writeFile(worldDirectory.resolve("level.dat").toFile(), nbt);
    }
}