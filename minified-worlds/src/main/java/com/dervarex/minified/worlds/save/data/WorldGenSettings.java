package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class WorldGenSettings {
    @Setter
    private int dataVersion;
    @Setter
    private long seed;
    @Setter
    private boolean bonusChest;
    @Setter
    private boolean generateStructures;
    private final Map<String, Dimension> dimensions = new LinkedHashMap<>();

    public static WorldGenSettings fromNbt(NbtCompound nbt) {
        WorldGenSettings result = new WorldGenSettings();
        result.dataVersion = nbt.getInt("DataVersion");

        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");

            if (data.has("seed")) {
                result.seed = data.getLong("seed");
            }
            if (data.has("bonus_chest")) {
                result.bonusChest = data.getByte("bonus_chest") != 0;
            }
            if (data.has("generate_structures")) {
                result.generateStructures = data.getByte("generate_structures") != 0;
            }

            if (data.has("dimensions")) {
                NbtCompound dimsCompound = data.getCompound("dimensions");
                for (Map.Entry<String, NbtTag> entry : dimsCompound.asMap().entrySet()) {
                    if (entry.getValue() instanceof NbtCompound dimCompound) {
                        result.dimensions.put(entry.getKey(), Dimension.fromNbt(dimCompound));
                    }
                }
            }
        }
        return result;
    }

    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();
        data.setLong("seed", seed);
        data.setByte("bonus_chest", (byte) (bonusChest ? 1 : 0));
        data.setByte("generate_structures", (byte) (generateStructures ? 1 : 0));

        NbtCompound dimsCompound = new NbtCompound();
        for (Map.Entry<String, Dimension> entry : dimensions.entrySet()) {
            dimsCompound.setCompound(entry.getKey(), entry.getValue().toNbt());
        }
        data.setCompound("dimensions", dimsCompound);

        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }

    @Getter
    @Setter
    public static class Dimension {
        @Nullable
        private String type;
        @Nullable
        private Generator generator;

        public static Dimension fromNbt(NbtCompound nbt) {
            Dimension dim = new Dimension();
            if (nbt.has("type")) {
                dim.type = nbt.getString("type");
            }
            if (nbt.has("generator")) {
                dim.generator = Generator.fromNbt(nbt.getCompound("generator"));
            }
            return dim;
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            if (type != null) {
                nbt.setString("type", type);
            }
            if (generator != null) {
                nbt.setCompound("generator", generator.toNbt());
            }
            return nbt;
        }
    }

    @Getter
    @Setter
    public static class Generator {
        @Nullable
        private String type;
        @Nullable
        private String settings;
        @Nullable
        private BiomeSource biomeSource;

        public static Generator fromNbt(NbtCompound nbt) {
            Generator gen = new Generator();
            if (nbt.has("type")) {
                gen.type = nbt.getString("type");
            }
            if (nbt.has("settings")) {
                gen.settings = nbt.getString("settings");
            }
            if (nbt.has("biome_source")) {
                gen.biomeSource = BiomeSource.fromNbt(nbt.getCompound("biome_source"));
            }
            return gen;
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            if (type != null) {
                nbt.setString("type", type);
            }
            if (settings != null) {
                nbt.setString("settings", settings);
            }
            if (biomeSource != null) {
                nbt.setCompound("biome_source", biomeSource.toNbt());
            }
            return nbt;
        }
    }

    @Getter
    @Setter
    public static class BiomeSource {
        @Nullable
        private String type;
        @Nullable
        private String preset;

        public static BiomeSource fromNbt(NbtCompound nbt) {
            BiomeSource bs = new BiomeSource();
            if (nbt.has("type")) {
                bs.type = nbt.getString("type");
            }
            if (nbt.has("preset")) {
                bs.preset = nbt.getString("preset");
            }
            return bs;
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            if (type != null) {
                nbt.setString("type", type);
            }
            if (preset != null) {
                nbt.setString("preset", preset);
            }
            return nbt;
        }
    }
}