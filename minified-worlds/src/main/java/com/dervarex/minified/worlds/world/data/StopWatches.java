package com.dervarex.minified.worlds.world.data;

import com.dervarex.minified.utils.nbt.tag.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class StopWatches {
    @Setter
    private int dataVersion;
    private final Map<String, Long> stopwatches = new LinkedHashMap<>();

    public static StopWatches fromNbt(NbtCompound nbt) {
        StopWatches result = new StopWatches();
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
     * Throws {@link ArithmeticException} if converting to a Long went wrong.
     */
    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();
        NbtCompound stopwatchesCompound = new NbtCompound();

        for (Map.Entry<String, Long> entry : stopwatches.entrySet()) {
            try {
                data.setInt(entry.getKey(), Math.toIntExact(entry.getValue()));
            } catch (ArithmeticException e) {
                throw new ArithmeticException(e.getMessage());
            }
        }

        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }
}
