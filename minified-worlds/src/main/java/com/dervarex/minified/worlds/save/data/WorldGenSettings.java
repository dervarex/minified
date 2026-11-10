package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apiguardian.api.API;

/**
 * The seed and generator of each dimension, from world_gen_settings.dat
 */
@Getter
@API(status = API.Status.STABLE)
public class WorldGenSettings {
    /**
     * Creates empty world gen settings
     */
    public WorldGenSettings() {
    }

    /** The data version of the Minecraft version that wrote it */
    @Setter
    private int dataVersion;
    /** The world seed */
    @Setter
    private long seed;
    /** If a bonus chest spawns */
    @Setter
    private boolean bonusChest;
    /** If structures generate */
    @Setter
    private boolean generateStructures;
    /** The dimensions by id */
    private final Map<String, Dimension> dimensions = new LinkedHashMap<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the settings from NBT
     * @param nbt the content of world_gen_settings.dat
     * @return the settings
     */
    public static WorldGenSettings fromNbt(NbtCompound nbt) {
        WorldGenSettings result = new WorldGenSettings();
        result.raw = nbt.copy();
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

    /** {@return the settings as NBT, like in world_gen_settings.dat} */
    public NbtCompound toNbt() {
        NbtCompound root = RawNbt.copyOrNew(raw);
        NbtCompound data = RawNbt.child(root, "data");
        data.setLong("seed", seed);
        data.setByte("bonus_chest", (byte) (bonusChest ? 1 : 0));
        data.setByte("generate_structures", (byte) (generateStructures ? 1 : 0));

        NbtCompound dimsCompound = RawNbt.child(data, "dimensions");
        RawNbt.removeMissing(dimsCompound, dimensions.keySet(), tag -> tag instanceof NbtCompound);
        for (Map.Entry<String, Dimension> entry : dimensions.entrySet()) {
            dimsCompound.setCompound(entry.getKey(), entry.getValue().toNbt());
        }
        root.setInt("DataVersion", dataVersion);
        return root;
    }

    /**
     * The type and generator of one dimension
     */
    @Getter
    @Setter
    public static class Dimension {
        /**
         * Creates an empty dimension
         */
        public Dimension() {
        }

        /** The dimension type */
        @Nullable
        private String type;
        /** The generator */
        @Nullable
        private Generator generator;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private NbtCompound raw;

        /**
         * Reads a dimension from NBT
         * @param nbt the dimension's NBT
         * @return the dimension
         */
        public static Dimension fromNbt(NbtCompound nbt) {
            Dimension dim = new Dimension();
            dim.raw = nbt.copy();
            if (nbt.has("type")) {
                dim.type = nbt.getString("type");
            }
            if (nbt.has("generator")) {
                dim.generator = Generator.fromNbt(nbt.getCompound("generator"));
            }
            return dim;
        }

        /** {@return the dimension as NBT} */
        public NbtCompound toNbt() {
            NbtCompound nbt = RawNbt.copyOrNew(raw);
            if (type != null) {
                nbt.setString("type", type);
            } else {
                nbt.remove("type");
            }
            if (generator != null) {
                nbt.setCompound("generator", generator.toNbt());
            } else {
                nbt.remove("generator");
            }
            return nbt;
        }
    }

    /**
     * The generator of a dimension
     */
    @Getter
    @Setter
    public static class Generator {
        /**
         * Creates an empty generator
         */
        public Generator() {
        }

        /** The generator type */
        @Nullable
        private String type;
        /** The noise settings id, null if they're written in the file */
        @Nullable
        private String settings;
        /** Where the biomes come from */
        @Nullable
        private BiomeSource biomeSource;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private NbtCompound raw;

        /**
         * Reads a generator from NBT
         * @param nbt the generator's NBT
         * @return the generator
         */
        public static Generator fromNbt(NbtCompound nbt) {
            Generator gen = new Generator();
            gen.raw = nbt.copy();
            if (nbt.has("type")) {
                gen.type = nbt.getString("type");
            }
            gen.settings = RawNbt.stringOrNull(nbt, "settings");
            if (nbt.has("biome_source")) {
                gen.biomeSource = BiomeSource.fromNbt(nbt.getCompound("biome_source"));
            }
            return gen;
        }

        /** {@return the generator as NBT} */
        public NbtCompound toNbt() {
            NbtCompound nbt = RawNbt.copyOrNew(raw);
            if (type != null) {
                nbt.setString("type", type);
            } else {
                nbt.remove("type");
            }
            RawNbt.setStringOrKeep(nbt, "settings", settings);
            if (biomeSource != null) {
                nbt.setCompound("biome_source", biomeSource.toNbt());
            } else {
                nbt.remove("biome_source");
            }
            return nbt;
        }
    }

    /**
     * Where a generator gets its biomes from
     */
    @Getter
    @Setter
    public static class BiomeSource {
        /**
         * Creates an empty biome source
         */
        public BiomeSource() {
        }

        /** The biome source type */
        @Nullable
        private String type;
        /** The biome preset */
        @Nullable
        private String preset;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private NbtCompound raw;

        /**
         * Reads a biome source from NBT
         * @param nbt the biome source's NBT
         * @return the biome source
         */
        public static BiomeSource fromNbt(NbtCompound nbt) {
            BiomeSource bs = new BiomeSource();
            bs.raw = nbt.copy();
            if (nbt.has("type")) {
                bs.type = nbt.getString("type");
            }
            if (nbt.has("preset")) {
                bs.preset = nbt.getString("preset");
            }
            return bs;
        }

        /** {@return the biome source as NBT} */
        public NbtCompound toNbt() {
            NbtCompound nbt = RawNbt.copyOrNew(raw);
            if (type != null) {
                nbt.setString("type", type);
            } else {
                nbt.remove("type");
            }
            if (preset != null) {
                nbt.setString("preset", preset);
            } else {
                nbt.remove("preset");
            }
            return nbt;
        }
    }
}