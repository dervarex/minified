package com.dervarex.minified.worlds.chunk;

import com.dervarex.minified.utils.nbt.tag.*;
import com.dervarex.minified.worlds.block.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChunkSection {
    private final NbtCompound raw;
    private final byte sectionY;

    private final List<BlockState> palette;
    private final long[] data; // null if the whole section is just a single block type
    private final int bitsPerBlock;

    private final List<String> biomePalette;
    private final long[] biomeData; // shared reference, or null for a size-1 palette
    private final int bitsPerBiome;

    /** Parses an existing section (read from a chunk's sections list) */
    public ChunkSection(NbtCompound sectionTag) {
        this.raw = sectionTag;
        this.sectionY = sectionTag.getByte("Y");

        NbtCompound blockStates = sectionTag.getCompound("block_states");
        NbtList paletteList = blockStates.getList("palette");

        this.palette = new ArrayList<>();
        for (NbtTag entry : paletteList.elements()) {
            NbtCompound entryCompound = (NbtCompound) entry;
            String name = entryCompound.getString("Name");
            Map<String, String> properties = new LinkedHashMap<>();
            if (entryCompound.has("Properties")) {
                NbtCompound props = entryCompound.getCompound("Properties");
                for (Map.Entry<String, NbtTag> e : props.asMap().entrySet()) {
                    if (e.getValue() instanceof NbtString s) {
                        properties.put(e.getKey(), s.value());
                    }
                }
            }
            palette.add(new BlockState(name, properties));
        }

        this.bitsPerBlock = Math.max(4, 32 - Integer.numberOfLeadingZeros(Math.max(1, palette.size() - 1)));
        this.data = blockStates.has("data") ? blockStates.getLongArray("data") : null;

        if (sectionTag.has("biomes")) {
            NbtCompound biomes = sectionTag.getCompound("biomes");
            NbtList biomePaletteList = biomes.getList("palette");
            List<String> parsedBiomePalette = new ArrayList<>();
            for (NbtTag entry : biomePaletteList.elements()) {
                parsedBiomePalette.add(((NbtString) entry).value());
            }
            this.biomePalette = parsedBiomePalette;
            this.bitsPerBiome = bitsFor(biomePalette.size());
            this.biomeData = biomes.has("data") ? biomes.getLongArray("data") : null;
        } else {
            this.biomePalette = new ArrayList<>();
            this.bitsPerBiome = 0;
            this.biomeData = null;
        }
    }

    private ChunkSection(NbtCompound raw, byte sectionY,
                         List<BlockState> palette, long[] data, int bitsPerBlock,
                         List<String> biomePalette, long[] biomeData, int bitsPerBiome) {
        this.raw = raw;
        this.sectionY = sectionY;
        this.palette = palette;
        this.data = data;
        this.bitsPerBlock = bitsPerBlock;
        this.biomePalette = biomePalette;
        this.biomeData = biomeData;
        this.bitsPerBiome = bitsPerBiome;
    }

    private static int bitsFor(int paletteSize) {
        if (paletteSize <= 1) return 0;
        return 32 - Integer.numberOfLeadingZeros(paletteSize - 1);
    }

    public byte sectionY() { return sectionY; }
    public NbtCompound raw() { return raw; }

    private static int localBlockIndex(int x, int y, int z) {
        return (y & 15) * 256 + (z & 15) * 16 + (x & 15);
    }

    private static int localBiomeIndex(int x, int y, int z) {
        int bx = (x & 15) >> 2;
        int by = (y & 15) >> 2;
        int bz = (z & 15) >> 2;
        return by * 16 + bz * 4 + bx;
    }

    public BlockState getBlock(int x, int y, int z) {
        if (palette.size() == 1) return palette.get(0);
        int index = readPacked(data, bitsPerBlock, localBlockIndex(x, y, z));
        return palette.get(index);
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        int paletteIndex = palette.indexOf(state);
        if (paletteIndex == -1) {
            throw new UnsupportedOperationException(
                    "Block state " + state.name() + " is not yet in this section's palette - " +
                            "palette growth/repacking is not implemented yet");
        }
        if (palette.size() == 1) return; // only one possible block, nothing to write
        writePacked(data, bitsPerBlock, localBlockIndex(x, y, z), paletteIndex);
    }

    public String getBiome(int x, int y, int z) {
        if (biomePalette.isEmpty()) return null;
        if (biomePalette.size() == 1) return biomePalette.get(0);
        int index = readPacked(biomeData, bitsPerBiome, localBiomeIndex(x, y, z));
        return biomePalette.get(index);
    }

    public void setBiome(int x, int y, int z, String biome) {
        int index = biomePalette.indexOf(biome);
        if (index == -1) {
            throw new UnsupportedOperationException(
                    "Biome " + biome + " is not yet in this section's biome palette - " +
                            "palette repacking is not implemented yet");
        }
        if (biomePalette.size() == 1) return;
        writePacked(biomeData, bitsPerBiome, localBiomeIndex(x, y, z), index);
    }

    /**
     * @return the most common biome across this section's 4x4x4 biome grid.
     * Returns null if this section has no biome data (should never happen for
     * sections created via {@link createEmpty}, since those always get a biome).
     */
    public String predominantBiome() {
        if (biomePalette.isEmpty()) return null;
        if (biomePalette.size() == 1) return biomePalette.get(0);

        int[] counts = new int[biomePalette.size()];
        for (int i = 0; i < 64; i++) {
            counts[readPacked(biomeData, bitsPerBiome, i)]++;
        }
        int bestIndex = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[bestIndex]) bestIndex = i;
        }
        return biomePalette.get(bestIndex);
    }

    private static int readPacked(long[] array, int bitsPerEntry, int index) {
        if (bitsPerEntry == 0) return 0;
        long bitIndex = (long) index * bitsPerEntry;
        int longIndex = (int) (bitIndex / 64);
        int bitOffset = (int) (bitIndex % 64);

        long value = array[longIndex] >>> bitOffset;
        if (bitOffset + bitsPerEntry > 64) {
            int remaining = bitOffset + bitsPerEntry - 64;
            value |= array[longIndex + 1] << (bitsPerEntry - remaining);
        }
        return (int) (value & ((1L << bitsPerEntry) - 1));
    }

    private static void writePacked(long[] array, int bitsPerEntry, int index, int value) {
        if (bitsPerEntry == 0) return;
        long bitIndex = (long) index * bitsPerEntry;
        int longIndex = (int) (bitIndex / 64);
        int bitOffset = (int) (bitIndex % 64);

        long mask = (1L << bitsPerEntry) - 1;
        long v = value & mask;

        array[longIndex] &= ~(mask << bitOffset);
        array[longIndex] |= v << bitOffset;

        if (bitOffset + bitsPerEntry > 64) {
            int remaining = bitOffset + bitsPerEntry - 64;
            long remainingMask = (1L << remaining) - 1;
            array[longIndex + 1] &= ~remainingMask;
            array[longIndex + 1] |= v >>> (bitsPerEntry - remaining);
        }
    }

    /**
     * Builds a new, empty section at the given Y, with a single-block palette (air)
     *  and a single-entry biome palette (given biome).
     */
    public static ChunkSection createEmpty(byte sectionY, String biome) {
        NbtCompound raw = new NbtCompound();
        raw.setByte("Y", sectionY);

        NbtCompound blockStates = new NbtCompound();
        NbtList blockPalette = new NbtList((byte) 10); // TAG_Compound
        NbtCompound airEntry = new NbtCompound();
        airEntry.setString("Name", "minecraft:air");
        blockPalette.add(airEntry);
        blockStates.setList("palette", blockPalette);
        raw.setCompound("block_states", blockStates);

        NbtCompound biomes = new NbtCompound();
        NbtList biomePaletteList = new NbtList((byte) 8); // TAG_String
        biomePaletteList.add(new NbtString(biome));
        biomes.setList("palette", biomePaletteList);
        raw.setCompound("biomes", biomes);

        List<BlockState> palette = List.of(BlockState.of("minecraft:air"));
        List<String> biomePaletteJava = new ArrayList<>(List.of(biome));

        return new ChunkSection(raw, sectionY, palette, null, 4, biomePaletteJava, null, 0);
    }
}