package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apiguardian.api.API;

/**
 * The total ticks of each dimension, from world_clocks.dat
 */
@Getter
@API(status = API.Status.STABLE)
public class WorldClocks {
    /**
     * Creates empty world clocks
     */
    public WorldClocks() {
    }

    @Setter
    private int dataVersion;
    private final Map<String, Long> clocks = new LinkedHashMap<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the world clocks from NBT
     * @param nbt the content of world_clocks.dat
     * @return the world clocks
     */
    public static WorldClocks fromNbt(NbtCompound nbt) {
        WorldClocks result = new WorldClocks();
        result.raw = nbt.copy();
        result.dataVersion = nbt.getInt("DataVersion");

        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");
            for (Map.Entry<String, NbtTag> entry : data.asMap().entrySet()) {
                if (entry.getValue() instanceof NbtCompound dimensionCompound) {
                    if (dimensionCompound.has("total_ticks")) {
                        result.clocks.put(entry.getKey(), dimensionCompound.getLong("total_ticks"));
                    }
                }
            }
        }
        return result;
    }

    /** {@return the world clocks as NBT, like in world_clocks.dat} */
    public NbtCompound toNbt() {
        NbtCompound root = RawNbt.copyOrNew(raw);
        NbtCompound data = RawNbt.child(root, "data");
        RawNbt.removeMissing(data, clocks.keySet(), tag -> tag instanceof NbtCompound c && c.has("total_ticks"));

        for (Map.Entry<String, Long> entry : clocks.entrySet()) {
            RawNbt.child(data, entry.getKey()).setLong("total_ticks", entry.getValue());
        }
        root.setInt("DataVersion", dataVersion);
        return root;
    }
}