package com.dervarex.minified.worlds.chunk;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import com.dervarex.minified.worlds.block.BlockState;
import com.dervarex.minified.worlds.block.BlockStateFormat;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static Chunk chunkWithDataVersion(int dataVersion) {
        NbtCompound root = new NbtCompound();
        root.setInt("DataVersion", dataVersion);
        root.setInt("xPos", 0);
        root.setInt("zPos", 0);
        root.setList("sections", new NbtList((byte) 10));
        return new Chunk(root);
    }

    private static NbtCompound blockEntity(String id, int x, int y, int z) {
        NbtCompound blockEntity = new NbtCompound();
        blockEntity.setString("id", id);
        blockEntity.setInt("x", x);
        blockEntity.setInt("y", y);
        blockEntity.setInt("z", z);
        return blockEntity;
    }

    @Test
    void changedBlocksLetMinecraftRedoLightAndHeightmaps() {
        NbtCompound section = sectionNbt(0, "minecraft:stone", "minecraft:plains");
        section.setByteArray("BlockLight", new byte[2048]);
        section.setByteArray("SkyLight", new byte[2048]);
        Chunk chunk = chunkWithSections(0, 0, section);
        chunk.raw().setByte("isLightOn", (byte) 1);
        NbtCompound heightmaps = new NbtCompound();
        heightmaps.setLongArray("WORLD_SURFACE", new long[37]);
        chunk.raw().setCompound("Heightmaps", heightmaps);

        chunk.setBlock(1, 1, 1, BlockState.of("minecraft:dirt"));

        assertFalse(chunk.raw().getBoolean("isLightOn"));
        assertFalse(section.has("BlockLight"));
        assertFalse(section.has("SkyLight"));
        assertTrue(chunk.raw().getCompound("Heightmaps").asMap().isEmpty());
        assertEquals(Set.of(0), chunk.editedSections());
    }

    @Test
    void settingTheSameBlockAgainChangesNothing() {
        Chunk chunk = chunkWithSections(0, 0, sectionNbt(0, "minecraft:stone", "minecraft:plains"));
        chunk.raw().setByte("isLightOn", (byte) 1);

        chunk.setBlock(1, 1, 1, BlockState.of("minecraft:stone"));

        assertTrue(chunk.raw().getBoolean("isLightOn"));
        assertTrue(chunk.editedSections().isEmpty());
    }

    @Test
    void replacedBlocksTakeTheirBlockEntityAlong() {
        Chunk chunk = chunkWithSections(2, -1, sectionNbt(0, "minecraft:chest", "minecraft:plains"));
        NbtList blockEntities = new NbtList((byte) 10);
        blockEntities.add(blockEntity("minecraft:chest", 37, 3, -9));
        blockEntities.add(blockEntity("minecraft:chest", 38, 3, -9));
        chunk.raw().setList("block_entities", blockEntities);

        chunk.setBlock(5, 3, 7, new BlockState("minecraft:chest", Map.of("facing", "east")));
        assertEquals(2, chunk.raw().getList("block_entities").size(), "a chest that was turned around keeps its items");

        chunk.setBlock(5, 3, 7, BlockState.of("minecraft:stone"));
        List<NbtTag> left = chunk.raw().getList("block_entities").elements();
        assertEquals(1, left.size());
        assertEquals(38, ((NbtCompound) left.getFirst()).getInt("x"));
    }

    @Test
    void newBlocksAreWrittenTheWayTheDataVersionExpects() {
        Chunk current = chunkWithDataVersion(5023);
        current.setBlock(0, 0, 0, BlockState.of("minecraft:stone"));
        List<NbtTag> currentPalette = ((NbtCompound) current.raw().getList("sections").elements().getFirst())
                .getCompound("block_states").getList("palette").elements();
        assertEquals(List.of(new NbtString("minecraft:air"), new NbtString("minecraft:stone")), currentPalette);

        Chunk older = chunkWithDataVersion(4671);
        older.setBlock(0, 0, 0, BlockState.of("minecraft:stone"));
        List<NbtTag> olderPalette = ((NbtCompound) older.raw().getList("sections").elements().getFirst())
                .getCompound("block_states").getList("palette").elements();
        assertEquals("minecraft:stone", ((NbtCompound) olderPalette.get(1)).getString("Name"));
        assertEquals(BlockStateFormat.NAME_PROPERTIES, older.blockStateFormat());
    }
}
