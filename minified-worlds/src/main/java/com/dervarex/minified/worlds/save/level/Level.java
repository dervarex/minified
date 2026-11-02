package com.dervarex.minified.worlds.save.level;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import org.apiguardian.api.API;

/**
 * level.dat
 * Null means it wasn't in the file (and won't be after saving either),
 * stuff we don't know about (version_history, mod data, ...) is left alone
 */
@Setter
@Getter
@API(status = API.Status.STABLE)
public class Level {
    @Nullable
    private Difficulty difficulty;
    private boolean hardcore;
    private boolean locked;
    @Nullable
    private String[] enabledDatapacks;
    @Nullable
    private String[] disabledDatapacks;
    @Nullable
    private Spawn spawn;
    @Nullable
    private Version version;
    @Nullable
    private String[] enabledFeatures; // only there when experiments are turned on
    @Nullable
    private String[] serverBrands;
    private boolean allowCommands;
    private int dataVersion;
    private int gameType;
    private byte initialized;
    private long lastPlayed;
    @Nullable
    private String levelName;
    private long time;
    private int nbtVersion;
    private boolean modded;
    @Nullable
    private UUID singleplayerUuid;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;


    public Level() {}

    public static Level fromNbt(NbtCompound nbt) {
        NbtCompound data = nbt.getCompound("Data");
        Level level = new Level();
        level.raw = nbt.copy();

        if (data.has("difficulty_settings")) {
            NbtCompound difficultySettings = data.getCompound("difficulty_settings");
            if (difficultySettings.has("difficulty")) {
                level.difficulty = Difficulty.valueOf(difficultySettings.getString("difficulty"));
            }
            level.hardcore = difficultySettings.has("hardcore") && difficultySettings.getBoolean("hardcore");
            level.locked = difficultySettings.has("locked") && difficultySettings.getBoolean("locked");
        }

        if (data.has("DataPacks")) {
            NbtCompound dataPacks = data.getCompound("DataPacks");
            level.enabledDatapacks = nbtListToStringArray(dataPacks, "Enabled");
            level.disabledDatapacks = nbtListToStringArray(dataPacks, "Disabled");
        }

        if (data.has("spawn")) level.spawn = Spawn.fromNbt(data.getCompound("spawn"));
        if (data.has("Version")) level.version = Version.fromNbt(data.getCompound("Version"));

        level.enabledFeatures = nbtListToStringArray(data, "enabled_features");
        level.serverBrands = nbtListToStringArray(data, "ServerBrands");

        level.allowCommands = data.has("allowCommands") && data.getBoolean("allowCommands");
        if (data.has("DataVersion")) level.dataVersion = data.getInt("DataVersion");
        if (data.has("GameType")) level.gameType = data.getInt("GameType");
        if (data.has("initialized")) level.initialized = data.getByte("initialized");
        if (data.has("LastPlayed")) level.lastPlayed = data.getLong("LastPlayed");
        if (data.has("LevelName")) level.levelName = data.getString("LevelName");
        if (data.has("Time")) level.time = data.getLong("Time");
        if (data.has("version")) level.nbtVersion = data.getInt("version");
        level.modded = data.has("WasModded") && data.getBoolean("WasModded");

        if (data.has("singleplayer_uuid")) level.singleplayerUuid = intArrayToUuid(data.getIntArray("singleplayer_uuid"));

        return level;
    }

    /**
     * Everything {@link #fromNbt} read, with your changes on top
     */
    public NbtCompound toNbt() {
        NbtCompound root = raw != null ? raw.copy() : new NbtCompound();
        NbtCompound data = child(root, "Data");

        if (difficulty != null || data.has("difficulty_settings")) {
            NbtCompound difficultySettings = child(data, "difficulty_settings");
            if (difficulty != null) difficultySettings.setString("difficulty", difficulty.name());
            difficultySettings.setBoolean("hardcore", hardcore);
            difficultySettings.setBoolean("locked", locked);
        }

        if (enabledDatapacks != null || disabledDatapacks != null || data.has("DataPacks")) {
            NbtCompound dataPacks = child(data, "DataPacks");
            putStrings(dataPacks, "Enabled", enabledDatapacks);
            putStrings(dataPacks, "Disabled", disabledDatapacks);
        }

        if (spawn != null) data.setCompound("spawn", spawn.toNbt());
        else data.remove("spawn");
        if (version != null) data.setCompound("Version", version.toNbt());
        else data.remove("Version");

        putStrings(data, "enabled_features", enabledFeatures);
        putStrings(data, "ServerBrands", serverBrands);

        data.setBoolean("allowCommands", allowCommands);
        data.setInt("DataVersion", dataVersion);
        data.setInt("GameType", gameType);
        data.setByte("initialized", initialized);
        data.setLong("LastPlayed", lastPlayed);
        if (levelName != null) data.setString("LevelName", levelName);
        else data.remove("LevelName");
        data.setLong("Time", time);
        data.setInt("version", nbtVersion);
        data.setBoolean("WasModded", modded);

        if (singleplayerUuid != null) data.setIntArray("singleplayer_uuid", uuidToIntArray(singleplayerUuid));
        else data.remove("singleplayer_uuid");

        return root;
    }

    // getOrCreate, basically
    private static NbtCompound child(NbtCompound parent, String key) {
        if (parent.get(key).orElse(null) instanceof NbtCompound existing) return existing;
        NbtCompound created = new NbtCompound();
        parent.setCompound(key, created);
        return created;
    }

    private static String[] nbtListToStringArray(NbtCompound compound, String key) {
        if (!compound.has(key)) return null;
        return compound.getList(key).elements().stream()
                .map(NbtString.class::cast)
                .map(NbtString::value)
                .toArray(String[]::new);
    }

    private static void putStrings(NbtCompound compound, String key, String[] values) {
        if (values == null) {
            compound.remove(key);
            return;
        }
        NbtList list = new NbtList((byte) 8); // TAG_String
        for (String value : values) {
            list.add(new NbtString(value));
        }
        compound.setList(key, list);
    }

    private static UUID intArrayToUuid(int[] ints) {
        long mostSigBits = ((long) ints[0] << 32) | (ints[1] & 0xFFFFFFFFL);
        long leastSigBits = ((long) ints[2] << 32) | (ints[3] & 0xFFFFFFFFL);
        return new UUID(mostSigBits, leastSigBits);
    }

    private static int[] uuidToIntArray(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        return new int[] {
                (int) (msb >> 32),
                (int) msb,
                (int) (lsb >> 32),
                (int) lsb
        };
    }
}