package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
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
    private final Map<String, long[]> sequences = new LinkedHashMap<>();

    public static RandomSequences fromNbt(NbtCompound nbt) {
        RandomSequences result = new RandomSequences();
        result.dataVersion = nbt.getInt("DataVersion");
        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");
            result.salt = data.getInt("salt");
            if (data.has("sequences")) {
                NbtCompound seqCompound = data.getCompound("sequences");
                for (Map.Entry<String, NbtTag> entry : seqCompound.asMap().entrySet()) {
                    if (entry.getValue() instanceof NbtCompound sequenceCompound && sequenceCompound.has("source")) {
                        result.sequences.put(entry.getKey(), sequenceCompound.getLongArray("source"));
                    }
                }
            }
        }
        return result;
    }
    public NbtCompound toNbt() {
        NbtCompound sequencesCompound = new NbtCompound();
        for (Map.Entry<String, long[]> entry : sequences.entrySet()) {
            NbtCompound sequenceCompound = new NbtCompound();
            sequenceCompound.setLongArray("source", entry.getValue());
            sequencesCompound.setCompound(entry.getKey(), sequenceCompound);
        }
        NbtCompound data = new NbtCompound();
        data.setInt("salt", salt);
        data.setCompound("sequences", sequencesCompound);
        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }

    public void putSequence(String key, long[] value) {
        sequences.put(key, value);
    }
    public void removeSequence(String key) {
        sequences.remove(key);
    }
}
