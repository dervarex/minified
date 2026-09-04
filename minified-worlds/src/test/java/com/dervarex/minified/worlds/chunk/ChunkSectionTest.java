package com.dervarex.minified.worlds.chunk;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import com.dervarex.minified.worlds.block.BlockState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkSectionTest {

    private static NbtCompound blockEntry(String name) {
        NbtCompound entry = new NbtCompound();
        entry.setString("Name", name);
        return entry;
    }

    private static ChunkSection singlePaletteSection(byte y, String block, String biome) {
        NbtCompound raw = new NbtCompound();
        raw.setByte("Y", y);

        NbtCompound blockStates = new NbtCompound();
        NbtList palette = new NbtList((byte) 10);
        palette.add(blockEntry(block));
        blockStates.setList("palette", palette);
        raw.setCompound("block_states", blockStates);

        NbtCompound biomes = new NbtCompound();
        NbtList biomePalette = new NbtList((byte) 8);
        biomePalette.add(new NbtString(biome));
        biomes.setList("palette", biomePalette);
        raw.setCompound("biomes", biomes);

        return new ChunkSection(raw);
    }

    @Test
    void testDefaultBlockIsSameEverywhere() {
        ChunkSection section = singlePaletteSection((byte) 4, "minecraft:stone", "minecraft:plains");
        assertEquals(BlockState.of("minecraft:stone"), section.getBlock(0, 0, 0));
        assertEquals(BlockState.of("minecraft:stone"), section.getBlock(15, 15, 15));
        assertEquals(4, section.sectionY());
    }

    @Test
    void testDefaultBiomeIsSameEverywhere() {
        ChunkSection section = singlePaletteSection((byte) 0, "minecraft:air", "minecraft:desert");
        assertEquals("minecraft:desert", section.getBiome(0, 0, 0));
        assertEquals("minecraft:desert", section.predominantBiome());
    }

    @Test
    void testCreateEmptySection() {
        ChunkSection section = ChunkSection.createEmpty((byte) 2, "minecraft:forest");
        assertEquals(BlockState.of("minecraft:air"), section.getBlock(3, 5, 9));
        assertEquals("minecraft:forest", section.getBiome(3, 5, 9));
        assertEquals(2, section.sectionY());
    }

    @Test
    void testSetSingleBlock() {
        ChunkSection section = ChunkSection.createEmpty((byte) 0, "minecraft:plains");
        BlockState stone = BlockState.of("minecraft:stone");

        section.setBlock(1, 2, 3, stone);

        assertEquals(stone, section.getBlock(1, 2, 3));
        // neighbouring block, untouched, should still be air
        assertEquals(BlockState.of("minecraft:air"), section.getBlock(0, 0, 0));
    }

    @Test
    void testSetBlocksInCorners() {
        ChunkSection section = ChunkSection.createEmpty((byte) 0, "minecraft:plains");
        BlockState stone = BlockState.of("minecraft:stone");
        BlockState dirt = BlockState.of("minecraft:dirt");

        section.setBlock(0, 0, 0, stone);
        section.setBlock(15, 15, 15, dirt);

        assertEquals(stone, section.getBlock(0, 0, 0));
        assertEquals(dirt, section.getBlock(15, 15, 15));
        assertEquals(BlockState.of("minecraft:air"), section.getBlock(1, 1, 1));
    }

    @Test
    void testSetManyDifferentBlocks() {
        ChunkSection section = ChunkSection.createEmpty((byte) 0, "minecraft:plains");

        // air (index 0) + 17 new block types = 18 total, forcing a repack past 4 bits (16 values)
        for (int i = 0; i < 17; i++) {
            section.setBlock(i % 16, 0, i / 16, BlockState.of("minecraft:block_" + i));
        }

        for (int i = 0; i < 17; i++) {
            assertEquals(BlockState.of("minecraft:block_" + i), section.getBlock(i % 16, 0, i / 16),
                    "block_" + i + " should survive the repack");
        }
        // never touched, should still be air
        assertEquals(BlockState.of("minecraft:air"), section.getBlock(0, 1, 0));
    }

    @Test
    void testSetSameBlockMultipleTimes() {
        ChunkSection section = ChunkSection.createEmpty((byte) 0, "minecraft:plains");
        BlockState stone = BlockState.of("minecraft:stone");

        section.setBlock(0, 0, 0, stone);
        section.setBlock(1, 0, 0, stone);
        section.setBlock(2, 0, 0, stone);

        assertEquals(stone, section.getBlock(0, 0, 0));
        assertEquals(stone, section.getBlock(1, 0, 0));
        assertEquals(stone, section.getBlock(2, 0, 0));
    }

    @Test
    void testSetBiomeInOneSpot() {
        ChunkSection section = ChunkSection.createEmpty((byte) 0, "minecraft:plains");

        section.setBiome(0, 0, 0, "minecraft:desert");

        assertEquals("minecraft:desert", section.getBiome(0, 0, 0));
        // a different 4x4x4 biome quadrant, untouched
        assertEquals("minecraft:plains", section.getBiome(15, 15, 15));
    }

    @Test
    void testFindMainBiome() {
        ChunkSection section = ChunkSection.createEmpty((byte) 0, "minecraft:plains");
        // there are 4x4x4=64 biome quadrants; overwrite some (48) with "desert"
        for (int by = 0; by < 4; by++) {
            for (int bz = 0; bz < 4; bz++) {
                for (int bx = 0; bx < 3; bx++) {
                    section.setBiome(bx * 4, by * 4, bz * 4, "minecraft:desert");
                }
            }
        }

        assertEquals("minecraft:desert", section.predominantBiome());
    }

    @Test
    void testBlockPropertiesAreSaved() {
        NbtCompound raw = new NbtCompound();
        raw.setByte("Y", (byte) 0);

        NbtCompound blockStates = new NbtCompound();
        NbtList palette = new NbtList((byte) 10);
        NbtCompound entry = blockEntry("minecraft:oak_stairs");
        NbtCompound props = new NbtCompound();
        props.setString("facing", "north");
        entry.setCompound("Properties", props);
        palette.add(entry);
        blockStates.setList("palette", palette);
        raw.setCompound("block_states", blockStates);

        ChunkSection section = new ChunkSection(raw);
        BlockState state = section.getBlock(0, 0, 0);

        assertEquals("minecraft:oak_stairs", state.name());
        assertEquals("north", state.properties().get("facing"));
    }
}
