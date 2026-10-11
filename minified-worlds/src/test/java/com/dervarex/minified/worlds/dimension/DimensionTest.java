package com.dervarex.minified.worlds.dimension;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import com.dervarex.minified.worlds.block.BlockState;
import com.dervarex.minified.worlds.chunk.Chunk;
import com.dervarex.minified.worlds.poi.PoiData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class DimensionTest {

    private static NbtCompound chunkNbt(int x, int z) {
        NbtCompound root = new NbtCompound();
        root.setInt("xPos", x);
        root.setInt("zPos", z);

        NbtCompound section = new NbtCompound();
        section.setByte("Y", (byte) 0);
        NbtCompound blockStates = new NbtCompound();
        NbtList palette = new NbtList((byte) 10);
        NbtCompound entry = new NbtCompound();
        entry.setString("Name", "minecraft:stone");
        palette.add(entry);
        blockStates.setList("palette", palette);
        section.setCompound("block_states", blockStates);

        NbtList sections = new NbtList((byte) 10);
        sections.add(section);
        root.setList("sections", sections);
        return root;
    }

    private void createRegionFolder(Path worldDir, String dimensionName, String category) throws IOException {
        Files.createDirectories(worldDir.resolve("dimensions").resolve("minecraft").resolve(dimensionName).resolve(category));
    }

    @Test
    void readingUngeneratedChunkReturnsNull(@TempDir Path worldDir) throws IOException {
        createRegionFolder(worldDir, "overworld", "region");
        Dimension dimension = new Overworld(worldDir.toFile());

        assertNull(dimension.readChunk(0, 0));
        dimension.close();
    }

    @Test
    void testSaveAndReadChunk(@TempDir Path worldDir) throws IOException {
        createRegionFolder(worldDir, "overworld", "region");
        Dimension dimension = new Overworld(worldDir.toFile());

        Chunk chunk = new Chunk(chunkNbt(5, -3));
        dimension.saveChunkData(chunk);

        NbtCompound raw = dimension.readChunkData(5, -3);
        assertNotNull(raw);
        assertEquals(5, raw.getInt("xPos"));
        assertEquals(-3, raw.getInt("zPos"));

        Chunk reread = dimension.readChunk(5, -3);
        assertEquals(5, reread.chunkX());
        assertEquals(-3, reread.chunkZ());
        dimension.close();
    }

    @Test
    void testSaveAndReadPoiData(@TempDir Path worldDir) throws IOException {
        createRegionFolder(worldDir, "the_nether", "poi");
        Dimension dimension = new Nether(worldDir.toFile());

        NbtCompound poiRoot = new NbtCompound();
        poiRoot.setCompound("Sections", new NbtCompound());
        PoiData poi = new PoiData(poiRoot);
        poi.addRecord(0, 1, 2, 3, "minecraft:nether_portal", 0);

        dimension.savePoiData(1, 1, poi);

        PoiData reread = dimension.readPoi(1, 1);
        assertNotNull(reread);
        assertEquals(1, reread.records(0).size());
        assertEquals("minecraft:nether_portal", reread.records(0).get(0).type());
        dimension.close();
    }

    @Test
    void testGetDimensionTypeFromFolderName() {
        File endFolder = new File("some/path/the_end");
        assertEquals(DimensionType.End, DimensionType.fromFolder(endFolder));

        File unknownFolder = new File("some/path/not_a_dimension");
        assertNull(DimensionType.fromFolder(unknownFolder));
    }

    @Test
    void savingAnEditedChunkRefreshesItsPointsOfInterest(@TempDir Path worldDir) throws IOException {
        createRegionFolder(worldDir, "overworld", "region");
        createRegionFolder(worldDir, "overworld", "poi");
        Dimension dimension = new Overworld(worldDir.toFile());
        NbtCompound poiRoot = new NbtCompound();
        poiRoot.setCompound("Sections", new NbtCompound());
        PoiData poi = new PoiData(poiRoot);
        poi.addRecord(0, 1, 2, 3, "minecraft:home", 0);
        poi.addRecord(1, 1, 20, 3, "minecraft:home", 0);
        dimension.savePoiData(0, 0, poi);

        Chunk chunk = new Chunk(chunkNbt(0, 0));
        chunk.setBlock(1, 2, 3, BlockState.of("minecraft:air")); // the bed in section 0 is gone
        dimension.saveChunkData(chunk);

        NbtCompound sections = dimension.readPoiData(0, 0).getCompound("Sections");
        assertFalse(sections.getCompound("0").getBoolean("Valid"), "only invalid sections get rebuilt by the game");
        assertTrue(sections.getCompound("1").getBoolean("Valid"));
        dimension.close();
    }

    @Test
    void refusesToWriteWhileTheGameHasTheWorldOpen(@TempDir Path worldDir) throws IOException {
        createRegionFolder(worldDir, "overworld", "region");
        Path lockFile = worldDir.resolve("session.lock");
        Files.write(lockFile, new byte[0]);
        Dimension dimension = new Overworld(worldDir.toFile());

        try (FileChannel game = FileChannel.open(lockFile, StandardOpenOption.WRITE); FileLock held = game.lock()) {
            assertThrows(IOException.class, () -> dimension.saveChunkData(new Chunk(chunkNbt(0, 0))));
        }
        assertNull(dimension.readChunk(0, 0));
        dimension.close();
    }
}
