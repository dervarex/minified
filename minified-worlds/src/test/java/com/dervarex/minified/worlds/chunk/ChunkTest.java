package com.dervarex.minified.worlds.chunk;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import com.dervarex.minified.worlds.block.BlockState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkTest {

    private static NbtCompound sectionNbt(int y, String block, String biome) {
        NbtCompound raw = new NbtCompound();
        raw.setByte("Y", (byte) y);

        NbtCompound blockStates = new NbtCompound();
        NbtList palette = new NbtList((byte) 10);
        NbtCompound entry = new NbtCompound();
        entry.setString("Name", block);
        palette.add(entry);
        blockStates.setList("palette", palette);
        raw.setCompound("block_states", blockStates);

        NbtCompound biomes = new NbtCompound();
        NbtList biomePalette = new NbtList((byte) 8);
        biomePalette.add(new NbtString(biome));
        biomes.setList("palette", biomePalette);
        raw.setCompound("biomes", biomes);

        return raw;
    }

    private static Chunk chunkWithSections(int chunkX, int chunkZ, NbtCompound... sections) {
        NbtCompound root = new NbtCompound();
        root.setInt("xPos", chunkX);
        root.setInt("zPos", chunkZ);
        NbtList sectionsList = new NbtList((byte) 10);
        for (NbtCompound section : sections) sectionsList.add(section);
        root.setList("sections", sectionsList);
        return new Chunk(root);
    }

    @Test
    void testLoadChunkAndSkipInvalidSections() {
        NbtCompound bogus = new NbtCompound();
        bogus.setByte("Y", (byte) 9);

        Chunk chunk = chunkWithSections(3, -2, bogus, sectionNbt(0, "minecraft:stone", "minecraft:plains"));

        assertEquals(3, chunk.chunkX());
        assertEquals(-2, chunk.chunkZ());
        assertEquals(BlockState.of("minecraft:stone"), chunk.getBlock(0, 0, 0));
    }

    @Test
    void testGetBlockInMissingSectionReturnsAir() {
        Chunk chunk = chunkWithSections(0, 0, sectionNbt(0, "minecraft:stone", "minecraft:plains"));
        // Section at y=200 does not exist, return air
        assertEquals(BlockState.of("minecraft:air"), chunk.getBlock(0, 200, 0));
    }

    @Test
    void testSetBlockInExistingSection() {
        Chunk chunk = chunkWithSections(0, 0, sectionNbt(0, "minecraft:stone", "minecraft:plains"));
        chunk.setBlock(1, 1, 1, BlockState.of("minecraft:dirt"));
        assertEquals(BlockState.of("minecraft:dirt"), chunk.getBlock(1, 1, 1));
    }

    @Test
    void testSetBlockInMissingSection() {
        Chunk chunk = chunkWithSections(0, 0);
        chunk.setBlock(5, 5, 5, BlockState.of("minecraft:stone"));
        assertEquals(BlockState.of("minecraft:stone"), chunk.getBlock(5, 5, 5));
        // Other blocks in this new section remain air
        assertEquals(BlockState.of("minecraft:air"), chunk.getBlock(0, 5, 0));
    }

    @Test
    void testCreateSectionFailsIfAlreadyExists() {
        Chunk chunk = chunkWithSections(0, 0, sectionNbt(0, "minecraft:stone", "minecraft:plains"));
        assertThrows(IllegalStateException.class, () -> chunk.createSection(0, "minecraft:plains"));
    }

    @Test
    void testCreateSectionWithCustomBiome() {
        Chunk chunk = chunkWithSections(0, 0, sectionNbt(0, "minecraft:stone", "minecraft:desert"));
        ChunkSection created = chunk.createSection(1, "minecraft:swamp");
        assertEquals("minecraft:swamp", created.predominantBiome());
    }

    @Test
    void testCreateSectionCopiesBiomeFromNeighbor() {
        Chunk chunk = chunkWithSections(0, 0, sectionNbt(0, "minecraft:stone", "minecraft:desert"));
        ChunkSection created = chunk.createSection(1, null);
        assertEquals("minecraft:desert", created.predominantBiome());
    }

    @Test
    void testCreateSectionUsesMajorityBiome() {
        Chunk chunk = chunkWithSections(0, 0,
                sectionNbt(0, "minecraft:stone", "minecraft:desert"),
                sectionNbt(2, "minecraft:stone", "minecraft:desert"));
        ChunkSection created = chunk.createSection(1, null);
        assertEquals("minecraft:desert", created.predominantBiome());
    }

    @Test
    void testCreateSectionFindsNearestBiome() {
        Chunk chunk = chunkWithSections(0, 0, sectionNbt(5, "minecraft:stone", "minecraft:jungle"));
        // Find nearest biome (from section 5)
        ChunkSection created = chunk.createSection(1, null);
        assertEquals("minecraft:jungle", created.predominantBiome());
    }

    @Test
    void testCreateSectionDefaultsToPlains() {
        Chunk chunk = chunkWithSections(0, 0);
        ChunkSection created = chunk.createSection(0, null);
        assertEquals("minecraft:plains", created.predominantBiome());
    }
}
