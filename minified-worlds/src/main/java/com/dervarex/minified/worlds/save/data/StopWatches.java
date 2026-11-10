package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apiguardian.api.API;

/**
 * The stopwatches of a world, from stopwatches.dat
 */
@Getter
@API(status = API.Status.STABLE)
public class StopWatches {
    /**
     * Creates empty stopwatches
     */
    public StopWatches() {
    }

    @Setter
    private int dataVersion;
    private final Map<String, Long> stopwatches = new LinkedHashMap<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the stopwatches from NBT
     * @param nbt the content of stopwatches.dat
     * @return the stopwatches
     */
    public static StopWatches fromNbt(NbtCompound nbt) {
        StopWatches result = new StopWatches();
        result.raw = nbt.copy();
        result.dataVersion = nbt.getInt("DataVersion");

        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");
            if(data.has("stopwatches")) {
                NbtCompound stopwatches = data.getCompound("stopwatches");
                for (Map.Entry<String, NbtTag> entry : stopwatches.asMap().entrySet()) {
                    String key = entry.getKey();
                    NbtTag tag = entry.getValue();

                    if(tag instanceof NbtLong longTag) {
                        result.stopwatches.put(key, longTag.value());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Converts the Stopwatches Object to an NbtCompound
     * @return the created NbtCompound
     */
    public NbtCompound toNbt() {
        NbtCompound root = RawNbt.copyOrNew(raw);
        NbtCompound data = RawNbt.child(root, "data");
        NbtCompound stopwatchesCompound = RawNbt.child(data, "stopwatches");
        RawNbt.removeMissing(stopwatchesCompound, stopwatches.keySet(), tag -> tag instanceof NbtLong);

        for (Map.Entry<String, Long> entry : stopwatches.entrySet()) {
            stopwatchesCompound.setLong(entry.getKey(), entry.getValue());
        }
        root.setInt("DataVersion", dataVersion);
        return root;
    }
}
