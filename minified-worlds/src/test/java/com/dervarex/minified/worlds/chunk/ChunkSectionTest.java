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

import static org.junit.jupiter.api.Assertions.*;

class ChunkSectionTest {

    private static final BlockState LOG = new BlockState("minecraft:oak_log", Map.of("axis", "x"));

    // plain ids, what 26.3 writes when nothing has properties
    private static NbtList idPalette(int size) {
        NbtList palette = new NbtList((byte) 8);
        for (int i = 0; i < size; i++) {
            palette.add(new NbtString("minecraft:block_" + i));
        }
        return palette;
    }

    private static NbtCompound sectionWith(NbtList palette, long[] data) {
        NbtCompound blockStates = new NbtCompound();
        blockStates.setList("palette", palette);
        if (data != null) blockStates.setLongArray("data", data);
        NbtCompound raw = new NbtCompound();
        raw.setByte("Y", (byte) 0);
        raw.setCompound("block_states", blockStates);
        return raw;
    }

    private static List<NbtTag> paletteOf(ChunkSection section) {
        return section.raw().getCompound("block_states").getList("palette").elements();
    }

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

    @Test
    void readsEntriesThatNeverSpanTwoLongs() {
        // 17 blocks = 5 bits = 12 per long, the top 4 bits stay empty
        long[] data = new long[342];
        data[0] = 3L << 55; // block 11 is the last one in the first long
        data[1] = 7L;       // block 12 is the first one in the second long
        ChunkSection section = new ChunkSection(sectionWith(idPalette(17), data));

        assertEquals(BlockState.of("minecraft:block_3"), section.getBlock(11, 0, 0));
        assertEquals(BlockState.of("minecraft:block_7"), section.getBlock(12, 0, 0));
        assertEquals(BlockState.of("minecraft:block_0"), section.getBlock(13, 0, 0));
    }

    @Test
    void writesEntriesThatNeverSpanTwoLongs() {
        ChunkSection section = ChunkSection.createEmpty((byte) 0, "minecraft:plains");
        for (int i = 1; i <= 16; i++) {
            section.setBlock(0, 15, i - 1, BlockState.of("minecraft:block_" + i)); // out of the way, 17 blocks in the palette now
        }
        section.setBlock(11, 0, 0, BlockState.of("minecraft:block_3"));
        section.setBlock(12, 0, 0, BlockState.of("minecraft:block_7"));

        long[] data = section.raw().getCompound("block_states").getLongArray("data");
        assertEquals(342, data.length);
        assertEquals(3L << 55, data[0]);
        assertEquals(7L, data[1]);
    }

    @Test
    void refusesDataThatDoesntFitThePalette() {
        // 320 is the pre 1.16 length, aka what we used to write. oops
        assertThrows(IllegalArgumentException.class, () -> new ChunkSection(sectionWith(idPalette(17), new long[320])));
        assertThrows(IllegalArgumentException.class, () -> new ChunkSection(sectionWith(idPalette(2), null)));
    }

    @Test
    void readsPalettesThatMixPlainIdsAndProperties() {
        NbtCompound stone = new NbtCompound();
        stone.setString("", "minecraft:stone");
        NbtCompound axis = new NbtCompound();
        axis.setString("axis", "x");
        NbtCompound log = new NbtCompound();
        log.setString("id", "minecraft:oak_log");
        log.setCompound("properties", axis);
        NbtList palette = new NbtList((byte) 10);
        palette.add(stone);
        palette.add(log);
        long[] data = new long[256];
        data[0] = 1L << 4;

        ChunkSection section = new ChunkSection(sectionWith(palette, data));

        assertEquals(BlockState.of("minecraft:stone"), section.getBlock(0, 0, 0));
        assertEquals(LOG, section.getBlock(1, 0, 0));
        assertEquals(BlockStateFormat.ID_PROPERTIES, section.blockStateFormat());
    }

    @Test
    void newBlocksAreWrittenLikeTheRestOfThePalette() {
        ChunkSection plain = new ChunkSection(sectionWith(idPalette(1), null));
        plain.setBlock(0, 0, 0, BlockState.of("minecraft:stone"));
        assertEquals(List.of(new NbtString("minecraft:block_0"), new NbtString("minecraft:stone")), paletteOf(plain));

        plain.setBlock(1, 0, 0, LOG);
        List<NbtTag> mixed = paletteOf(plain);
        assertEquals("minecraft:block_0", ((NbtCompound) mixed.get(0)).getString(""), "a list can't mix strings and compounds on disk");
        assertEquals("minecraft:stone", ((NbtCompound) mixed.get(1)).getString(""));
        assertEquals("minecraft:oak_log", ((NbtCompound) mixed.get(2)).getString("id"));
        assertEquals("x", ((NbtCompound) mixed.get(2)).getCompound("properties").getString("axis"));
        assertEquals(LOG, plain.getBlock(1, 0, 0));

        ChunkSection named = singlePaletteSection((byte) 0, "minecraft:air", "minecraft:plains");
        named.setBlock(0, 0, 0, LOG);
        NbtCompound namedLog = (NbtCompound) paletteOf(named).get(1);
        assertEquals("minecraft:oak_log", namedLog.getString("Name"));
        assertEquals("x", namedLog.getCompound("Properties").getString("axis"));
    }

    @Test
    void biomeDataHasTheLengthMinecraftExpects() {
        ChunkSection section = ChunkSection.createEmpty((byte) 0, "minecraft:plains");
        section.setBiome(0, 0, 0, "minecraft:desert");
        section.setBiome(4, 0, 0, "minecraft:forest");
        section.setBiome(8, 0, 0, "minecraft:swamp");
        section.setBiome(4, 4, 4, "minecraft:taiga"); // cell 21, the first one in the second long

        long[] data = section.raw().getCompound("biomes").getLongArray("data");
        assertEquals(4, data.length); // 5 biomes need 3 bits, 21 cells fit into a long
        assertEquals(4L, data[1]);
        assertEquals("minecraft:taiga", section.getBiome(4, 4, 4));
        assertEquals("minecraft:desert", section.getBiome(0, 0, 0));
        assertEquals("minecraft:plains", section.getBiome(12, 0, 0));
    }
}
