package com.dervarex.minified.worlds.world.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtIntArray;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class RandomSequences {
    @Setter
    private int dataVersion;
    @Setter
    private int salt;
    private final Map<String, int[]> sequences = new LinkedHashMap<>();

    public static RandomSequences fromNbt(NbtCompound nbt) {
        RandomSequences result = new RandomSequences();
        result.dataVersion = nbt.getInt("DataVersion");
        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");
            result.salt = data.getInt("Salt");
            if (data.has("sequences")) {
                NbtCompound seqCompound = data.getCompound("sequences");
                for (Map.Entry<String, NbtTag> entry : seqCompound.asMap().entrySet()) {
                    if (entry.getValue() instanceof NbtIntArray intArrayTag) {
                        result.sequences.put(entry.getKey(), intArrayTag.value());
                    }
                }
            }
        }
        return result;
    }
    public NbtCompound toNbt() {
        NbtCompound sequencesCompound = new NbtCompound();
        for (Map.Entry<String, int[]> entry : sequences.entrySet()) {
            sequencesCompound.setIntArray(entry.getKey(), entry.getValue());
        }
        NbtCompound data = new NbtCompound();
        data.setInt("Salt", salt);
        data.setCompound("sequences", sequencesCompound);
        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }

    public void putSequence(String key, int[] value) {
        sequences.put(key, value);
    }
    public void removeSequence(String key) {
        sequences.remove(key);
    }
}