package com.dervarex.minified.worlds.world.data;

import com.dervarex.minified.utils.nbt.tag.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class WorldClocks {
    @Setter
    private int dataVersion;
    private final Map<String, Long> clocks = new LinkedHashMap<>();

    public static WorldClocks fromNbt(NbtCompound nbt) {
        WorldClocks result = new WorldClocks();
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

    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();

        for (Map.Entry<String, Long> entry : clocks.entrySet()) {
            NbtCompound dimensionCompound = new NbtCompound();
            dimensionCompound.setLong("total_ticks", entry.getValue());
            data.setCompound(entry.getKey(), dimensionCompound);
        }

        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }
}