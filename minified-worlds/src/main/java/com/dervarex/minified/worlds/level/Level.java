package com.dervarex.minified.worlds.level;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class Level {
    private Difficulty difficulty;
    private boolean hardcore;
    private boolean locked;
    private String[] enabledDatapacks;
    private String[] disabledDatapacks;
    private Spawn spawn;
    private Version version;
    private String[] enabledFeatures;
    private String[] serverBrands;
    private boolean allowCommands;
    private int dataVersion;
    private int gameType;
    private byte initialized;
    private long lastPlayed;
    private String levelName;
    private int time;
    private int nbtVersion;
    private boolean modded;
    private UUID singleplayerUuid;


    public Level() {}

    public static Level fromNbt(NbtCompound nbt) {
        NbtCompound data = nbt.getCompound("Data");
        Level level = new Level();

        NbtCompound difficultySettings = data.getCompound("difficulty_settings");
        level.difficulty = Difficulty.valueOf(difficultySettings.getString("difficulty"));
        level.hardcore = difficultySettings.getBoolean("hardcore");
        level.locked = difficultySettings.getBoolean("locked");

        NbtCompound dataPacks = data.getCompound("DataPacks");
        level.enabledDatapacks = nbtListToStringArray(dataPacks.getList("Enabled"));
        level.disabledDatapacks = nbtListToStringArray(dataPacks.getList("Disabled"));

        level.spawn = Spawn.fromNbt(data.getCompound("spawn"));
        level.version = Version.fromNbt(data.getCompound("Version"));

        level.enabledFeatures = nbtListToStringArray(data.getList("enabled_features"));
        level.serverBrands = nbtListToStringArray(data.getList("ServerBrands"));

        level.allowCommands = data.getBoolean("allowCommands");
        level.dataVersion = data.getInt("DataVersion");
        level.gameType = data.getInt("GameType");
        level.initialized = data.getByte("initialized");
        level.lastPlayed = data.getLong("LastPlayed");
        level.levelName = data.getString("LevelName");
        level.time = data.getInt("Time");
        level.nbtVersion = data.getInt("version");
        level.modded = data.getBoolean("WasModded");

        level.singleplayerUuid = intArrayToUuid(data.getIntArray("singleplayer_uuid").value());

        return level;
    }

    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();

        NbtCompound difficultySettings = new NbtCompound();
        difficultySettings.setString("difficulty", difficulty.name());
        difficultySettings.setBoolean("hardcore", hardcore);
        difficultySettings.setBoolean("locked", locked);
        data.setCompound("difficulty_settings", difficultySettings);

        NbtCompound dataPacks = new NbtCompound();
        dataPacks.setList("Enabled", stringArrayToNbtList(enabledDatapacks));
        dataPacks.setList("Disabled", stringArrayToNbtList(disabledDatapacks));
        data.setCompound("DataPacks", dataPacks);

        data.setCompound("spawn", spawn.toNbt());
        data.setCompound("Version", version.toNbt());

        data.setList("enabled_features", stringArrayToNbtList(enabledFeatures));
        data.setList("ServerBrands", stringArrayToNbtList(serverBrands));

        data.setBoolean("allowCommands", allowCommands);
        data.setInt("DataVersion", dataVersion);
        data.setInt("GameType", gameType);
        data.setByte("initialized", initialized);
        data.setLong("LastPlayed", lastPlayed);
        data.setString("LevelName", levelName);
        data.setInt("Time", time);
        data.setInt("version", nbtVersion);
        data.setBoolean("WasModded", modded);

        data.setIntArray("singleplayer_uuid", uuidToIntArray(singleplayerUuid));

        NbtCompound root = new NbtCompound();
        root.setCompound("Data", data);
        return root;
    }

    private static String[] nbtListToStringArray(NbtList list) {
        return list.elements().stream()
                .map(NbtString.class::cast)
                .map(NbtString::value)
                .toArray(String[]::new);
    }

    private static NbtList stringArrayToNbtList(String[] values) {
        NbtString probe = new NbtString(values.length > 0 ? values[0] : "");
        NbtList list = new NbtList(probe.id());
        for (String value : values) {
            list.add(new NbtString(value));
        }
        return list;
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