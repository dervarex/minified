package com.dervarex.minified.worlds.chunk;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import com.dervarex.minified.worlds.block.BlockState;
import com.dervarex.minified.worlds.block.BlockStateFormat;

import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;

/**
 * A 16x16x16 piece of a chunk, its blocks and its biomes (one per 4x4x4 cell)
 */
@API(status = API.Status.STABLE)
public class ChunkSection {
    private static final int BLOCKS = 4096;
    private static final int BIOMES = 64;

    private final NbtCompound raw;
    private final byte sectionY;
    private final BlockStateFormat format;

    private final List<BlockState> palette = new ArrayList<>();
    private long[] data; // null if the whole section is just a single block type
    private int bitsPerBlock;

    private final List<String> biomePalette = new ArrayList<>();
    private long[] biomeData; // shared reference, or null for a size 1 palette
    private int bitsPerBiome;

    /**
     * Parses an existing section (read from a chunk's sections list)
     * @throws IllegalArgumentException if a palette and its data don't add up
     */
    public ChunkSection(NbtCompound sectionTag) {
        this(sectionTag, null);
    }

    /**
     * Like {@link #ChunkSection(NbtCompound)}
     * @param format how new blocks get written, null to just copy whatever the palette does
     */
    public ChunkSection(NbtCompound sectionTag, BlockStateFormat format) {
        this.raw = sectionTag;
        this.sectionY = sectionTag.getByte("Y");

        NbtCompound blockStates = sectionTag.getCompound("block_states");
        NbtList paletteList = blockStates.getList("palette");
        for (NbtTag entry : paletteList.elements()) palette.add(BlockState.fromNbt(entry));
        if (palette.isEmpty()) throw new IllegalArgumentException("Section Y=" + sectionY + " has an empty block palette");
        this.format = format != null ? format : formatOf(paletteList);
        this.bitsPerBlock = bitsForBlocks(palette.size());
        this.data = bitsPerBlock == 0 ? null : packedData(blockStates, BLOCKS, bitsPerBlock, "block_states");

        if (sectionTag.has("biomes")) {
            NbtCompound biomes = sectionTag.getCompound("biomes");
            for (NbtTag entry : biomes.getList("palette").elements()) biomePalette.add(((NbtString) entry).value());
            this.bitsPerBiome = bitsForBiomes(biomePalette.size());
            this.biomeData = bitsPerBiome == 0 ? null : packedData(biomes, BIOMES, bitsPerBiome, "biomes");
        }
    }

    public byte sectionY() { return sectionY; }
    public NbtCompound raw() { return raw; }

    /**
     * @return how new blocks get written into the palette
     */
    public BlockStateFormat blockStateFormat() { return format; }

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
        if (data == null) return palette.get(0);
        return palette.get(readPacked(data, bitsPerBlock, localBlockIndex(x, y, z)));
    }

    /**
     * Sets a block in this section and nothing else, {@link Chunk#setBlock} also cleans up after itself
     */
    public void setBlock(int x, int y, int z, BlockState state) {
        int paletteIndex = palette.indexOf(state);
        if (paletteIndex == -1) {
            paletteIndex = addBlock(state);
        }
        if (data == null) return; // only one possible block, nothing to write
        writePacked(data, bitsPerBlock, localBlockIndex(x, y, z), paletteIndex);
    }

    public String getBiome(int x, int y, int z) {
        if (biomePalette.isEmpty()) return null;
        if (biomeData == null) return biomePalette.get(0);
        return biomePalette.get(readPacked(biomeData, bitsPerBiome, localBiomeIndex(x, y, z)));
    }

    public void setBiome(int x, int y, int z, String biome) {
        int index = biomePalette.indexOf(biome);
        if (index == -1) {
            index = addBiome(biome);
        }
        if (biomeData == null) return;
        writePacked(biomeData, bitsPerBiome, localBiomeIndex(x, y, z), index);
    }

    /**
     * Adds a state to the palette, repacks the data if it needs more bits now
     */
    private int addBlock(BlockState state) {
        palette.add(state);
        NbtCompound blockStatesTag = raw.getCompound("block_states");
        blockStatesTag.setList("palette", paletteTag(palette, format));

        int bits = bitsForBlocks(palette.size());
        if (bits != bitsPerBlock) {
            data = repack(data, bitsPerBlock, bits, BLOCKS);
            bitsPerBlock = bits;
            blockStatesTag.setLongArray("data", data);
        }
        return palette.size() - 1;
    }

    /**
     * {@link #addBlock}, but for biomes
     */
    private int addBiome(String biome) {
        biomePalette.add(biome);
        NbtCompound biomesTag = raw.has("biomes") ? raw.getCompound("biomes") : new NbtCompound();
        raw.setCompound("biomes", biomesTag);
        biomesTag.setList("palette", biomePaletteTag(biomePalette));

        int bits = bitsForBiomes(biomePalette.size());
        if (bits != bitsPerBiome) {
            biomeData = repack(biomeData, bitsPerBiome, bits, BIOMES);
            bitsPerBiome = bits;
            biomesTag.setLongArray("data", biomeData);
        }
        return biomePalette.size() - 1;
    }

    /**
     * @return the most common biome across this section's 4x4x4 biome grid.
     * Returns null if this section has no biome data (should never happen for
     * sections created via {@link #createEmpty}, since those always get a biome).
     */
    public String predominantBiome() {
        if (biomePalette.isEmpty()) return null;
        if (biomeData == null) return biomePalette.get(0);

        int[] counts = new int[biomePalette.size()];
        for (int i = 0; i < BIOMES; i++) {
            counts[readPacked(biomeData, bitsPerBiome, i)]++;
        }
        int bestIndex = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[bestIndex]) bestIndex = i;
        }
        return biomePalette.get(bestIndex);
    }

    private long[] packedData(NbtCompound container, int entries, int bits, String what) {
        long[] values = container.has("data") ? container.getLongArray("data") : null;
        int expected = longsFor(entries, bits);
        if (values == null || values.length != expected) {
            throw new IllegalArgumentException("Section Y=" + sectionY + ": the " + what + " palette needs " + expected
                    + " longs of data, found " + (values == null ? "none" : values.length));
        }
        return values;
    }

    private static BlockStateFormat formatOf(NbtList palette) {
        for (NbtTag entry : palette.elements()) {
            if (entry instanceof NbtCompound compound && compound.has("Name")) return BlockStateFormat.NAME_PROPERTIES;
        }
        return BlockStateFormat.ID_PROPERTIES;
    }

    private static NbtList paletteTag(List<BlockState> states, BlockStateFormat format) {
        List<NbtTag> entries = states.stream().map(format::toNbt).toList();
        boolean mixed = entries.stream().anyMatch(NbtString.class::isInstance) && entries.stream().anyMatch(NbtCompound.class::isInstance);
        NbtList list = new NbtList(mixed ? (byte) 10 : entries.getFirst().id());
        for (NbtTag entry : entries) {
            if (mixed && entry instanceof NbtString) {
                // strings and compounds in one list? not on disk, so the strings get wrapped like this. thanks mojang
                NbtCompound wrapper = new NbtCompound();
                wrapper.put("", entry);
                list.add(wrapper);
            } else {
                list.add(entry);
            }
        }
        return list;
    }

    private static NbtList biomePaletteTag(List<String> biomes) {
        NbtList list = new NbtList((byte) 8); // TAG_String
        for (String biome : biomes) {
            list.add(new NbtString(biome));
        }
        return list;
    }

    // blocks never go below 4 bits, don't ask me why, biomes take what they need
    private static int bitsForBlocks(int paletteSize) {
        return paletteSize <= 1 ? 0 : Math.max(4, ceilLog2(paletteSize));
    }

    private static int bitsForBiomes(int paletteSize) {
        return paletteSize <= 1 ? 0 : ceilLog2(paletteSize);
    }

    private static int ceilLog2(int value) {
        return 32 - Integer.numberOfLeadingZeros(value - 1);
    }

    // since 1.16 an entry never spans two longs, the leftover bits at the top just sit there doing nothing
    private static int longsFor(int entries, int bits) {
        int perLong = 64 / bits;
        return (entries + perLong - 1) / perLong;
    }

    private static int readPacked(long[] array, int bitsPerEntry, int index) {
        int perLong = 64 / bitsPerEntry;
        int shift = (index % perLong) * bitsPerEntry;
        return (int) ((array[index / perLong] >>> shift) & ((1L << bitsPerEntry) - 1));
    }

    private static void writePacked(long[] array, int bitsPerEntry, int index, int value) {
        int perLong = 64 / bitsPerEntry;
        int shift = (index % perLong) * bitsPerEntry;
        long mask = ((1L << bitsPerEntry) - 1) << shift;
        array[index / perLong] = (array[index / perLong] & ~mask) | (((long) value << shift) & mask);
    }

    private static long[] repack(long[] array, int fromBits, int toBits, int entries) {
        long[] repacked = new long[longsFor(entries, toBits)];
        if (array != null) {
            for (int i = 0; i < entries; i++) {
                writePacked(repacked, toBits, i, readPacked(array, fromBits, i));
            }
        }
        return repacked;
    }

    /**
     * Builds a new, empty section at the given Y, with a single-block palette (air)
     *  and a single-entry biome palette (given biome), in the current block state format
     */
    public static ChunkSection createEmpty(byte sectionY, String biome) {
        return createEmpty(sectionY, biome, BlockStateFormat.ID_PROPERTIES);
    }

    /**
     * Like {@link #createEmpty(byte, String)}
     * @param format should match the chunk's DataVersion, see {@link BlockStateFormat#forDataVersion}
     */
    public static ChunkSection createEmpty(byte sectionY, String biome, BlockStateFormat format) {
        NbtCompound raw = new NbtCompound();
        raw.setByte("Y", sectionY);

        NbtCompound blockStates = new NbtCompound();
        blockStates.setList("palette", paletteTag(List.of(BlockState.of("minecraft:air")), format));
        raw.setCompound("block_states", blockStates);

        NbtCompound biomes = new NbtCompound();
        biomes.setList("palette", biomePaletteTag(List.of(biome)));
        raw.setCompound("biomes", biomes);

        return new ChunkSection(raw, format);
    }
}