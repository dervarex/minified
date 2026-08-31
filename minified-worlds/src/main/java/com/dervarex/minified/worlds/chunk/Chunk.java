package com.dervarex.minified.worlds.chunk;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import com.dervarex.minified.worlds.block.BlockState;

import java.util.HashMap;
import java.util.Map;

public class Chunk {
    private final NbtCompound root;
    private final int chunkX;
    private final int chunkZ;
    private final Map<Byte, ChunkSection> sections = new HashMap<>();

    public Chunk(NbtCompound root) {
        this.root = root;
        this.chunkX = root.getInt("xPos");
        this.chunkZ = root.getInt("zPos");

        for (NbtTag tag : root.getList("sections").elements()) {
            NbtCompound sectionTag = (NbtCompound) tag;
            if (!sectionTag.has("block_states")) continue;
            ChunkSection section = new ChunkSection(sectionTag);
            sections.put(section.sectionY(), section);
        }
    }

    public int chunkX() { return chunkX; }
    public int chunkZ() { return chunkZ; }

    public BlockState getBlock(int x, int y, int z) {
        ChunkSection section = sections.get((byte) (y >> 4));
        return section == null ? BlockState.of("minecraft:air") : section.getBlock(x, y, z);
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        ChunkSection section = sections.get((byte) (y >> 4));
        if (section == null) {
            throw new UnsupportedOperationException(
                    "Section for y=" + y + " does not exist in this chunk, " +
                            "section creation is not implemented yet"); // todo implement section creation
        }
        section.setBlock(x, y, z, state);
    }

    public NbtCompound raw() { return root; }
}