package com.dervarex.minified.worlds.chunk;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import com.dervarex.minified.worlds.block.BlockState;
import com.dervarex.minified.worlds.block.BlockStateFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Chunk {
    private static final String DEFAULT_FALLBACK_BIOME = "minecraft:plains";

    private final NbtCompound root;
    private final NbtList sectionsList;
    private final int chunkX;
    private final int chunkZ;
    private final BlockStateFormat blockStateFormat;
    private final Map<Byte, ChunkSection> sections = new HashMap<>();
    private final Set<Integer> editedSections = new TreeSet<>();
    private boolean lightReset;

    /**
     * Wraps a chunk's NBT as it comes out of a region file
     * @throws IllegalArgumentException if a section's palette and data don't add up
     */
    public Chunk(NbtCompound root) {
        this.root = root;
        this.chunkX = root.getInt("xPos");
        this.chunkZ = root.getInt("zPos");
        this.sectionsList = root.getList("sections");
        BlockStateFormat dataVersionFormat = root.has("DataVersion") ? BlockStateFormat.forDataVersion(root.getInt("DataVersion")) : null;

        for (NbtTag tag : sectionsList.elements()) {
            NbtCompound sectionTag = (NbtCompound) tag;
            if (!sectionTag.has("block_states")) continue;
            ChunkSection section = new ChunkSection(sectionTag, dataVersionFormat);
            sections.put(section.sectionY(), section);
        }
        this.blockStateFormat = dataVersionFormat != null ? dataVersionFormat : sections.values().stream()
                .map(ChunkSection::blockStateFormat)
                .findFirst()
                .orElse(BlockStateFormat.ID_PROPERTIES);
    }

    public int chunkX() { return chunkX; }
    public int chunkZ() { return chunkZ; }
    public NbtCompound raw() { return root; }

    /**
     * @return how this chunk's palettes store blocks
     */
    public BlockStateFormat blockStateFormat() { return blockStateFormat; }

    /**
     * @return the Y of every section {@link #setBlock} changed something in
     */
    public Set<Integer> editedSections() { return Collections.unmodifiableSet(editedSections); }

    public BlockState getBlock(int x, int y, int z) {
        ChunkSection section = sections.get((byte) (y >> 4));
        return section == null ? BlockState.of("minecraft:air") : section.getBlock(x, y, z);
    }

    /**
     * Sets a block and automatically creates the target section
     * (as air, with a resolved biome) if it doesn't exist yet
     * Light and heightmaps get thrown out, Minecraft redoes them on load. A chest that turns into stone takes its
     * items with it, a chest you place gets no block entity though, the game makes one once it needs it
     */
    public void setBlock(int x, int y, int z, BlockState state) {
        byte sectionY = (byte) (y >> 4);
        ChunkSection section = sections.get(sectionY);
        if (section == null) {
            section = createSection(sectionY, null);
        }
        BlockState previous = section.getBlock(x, y, z);
        if (previous.equals(state)) return;
        section.setBlock(x, y, z, state);
        blockChanged(x, y, z, previous, state);
    }

    private void blockChanged(int x, int y, int z, BlockState previous, BlockState state) {
        editedSections.add(y >> 4);
        if (!lightReset) {
            root.setBoolean("isLightOn", false);
            for (NbtTag tag : sectionsList.elements()) {
                NbtCompound sectionTag = (NbtCompound) tag;
                sectionTag.remove("BlockLight");
                sectionTag.remove("SkyLight");
            }
            if (root.has("Heightmaps")) root.setCompound("Heightmaps", new NbtCompound());
            lightReset = true;
        }
        if (!previous.name().equals(state.name())) removeBlockEntity(x, y, z);
    }

    private void removeBlockEntity(int x, int y, int z) {
        if (!root.has("block_entities")) return;
        int worldX = (chunkX << 4) | (x & 15);
        int worldZ = (chunkZ << 4) | (z & 15);

        NbtList blockEntities = root.getList("block_entities");
        NbtList kept = new NbtList(blockEntities.elementId());
        for (NbtTag tag : blockEntities.elements()) {
            boolean here = tag instanceof NbtCompound blockEntity
                    && blockEntity.getInt("x") == worldX && blockEntity.getInt("y") == y && blockEntity.getInt("z") == worldZ;
            if (!here) kept.add(tag);
        }
        if (kept.size() != blockEntities.size()) root.setList("block_entities", kept);
    }

    /**
     * Creates a new, empty (air) section at the given Y.
     *
     * @param biome the biome to fill the new section with. If null, it looks at the
     *              section(s) directly above/below within this chunk, if both exist, their
     *              predominant biomes (calculated via {@link ChunkSection#predominantBiome}
     *              are tallied together and the overall most common one gets used;
     *              if only one exists, its predominant biome is used. If neither
     *              immediate neighbor exists, this searches outward for the nearest existing
     *              section in either direction. If this chunk has no sections at all yet,
     *              it falls back to "minecraft:plains".
     * @throws IllegalStateException if a section already exists at this Y
     */
    public ChunkSection createSection(int sectionY, String biome) {
        byte y = (byte) sectionY;
        if (sections.containsKey(y)) {
            throw new IllegalStateException("Section Y=" + y + " already exists in this chunk");
        }

        String resolvedBiome = biome != null ? biome : resolveNeighborBiome(y);

        ChunkSection section = ChunkSection.createEmpty(y, resolvedBiome, blockStateFormat);
        sections.put(y, section);
        sectionsList.add(section.raw());
        return section;
    }

    private String resolveNeighborBiome(byte y) {
        ChunkSection below = sections.get((byte) (y - 1));
        ChunkSection above = sections.get((byte) (y + 1));

        if (below != null || above != null) {
            Map<String, Integer> counts = new HashMap<>();
            if (below != null) tally(counts, below);
            if (above != null) tally(counts, above);
            return mostFrequent(counts);
        }

        for (int distance = 2; distance <= 32; distance++) {
            ChunkSection farBelow = sections.get((byte) (y - distance));
            ChunkSection farAbove = sections.get((byte) (y + distance));
            if (farBelow != null) return farBelow.predominantBiome();
            if (farAbove != null) return farAbove.predominantBiome();
        }

        return DEFAULT_FALLBACK_BIOME;
    }

    private void tally(Map<String, Integer> counts, ChunkSection section) {
        String biome = section.predominantBiome();
        if (biome != null) counts.merge(biome, 1, Integer::sum);
    }

    private String mostFrequent(Map<String, Integer> counts) {
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_FALLBACK_BIOME);
    }
}