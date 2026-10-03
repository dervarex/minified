package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionFileTest {

    @TempDir
    Path tempDir;

    @Test
    void writtenChunksCanBeReadAfterReopening() throws IOException {
        File file = tempDir.resolve("r.-1.-1.mca").toFile();
        try (RegionFile region = RegionFile.create(file)) {
            region.writeChunk(-32, -32, chunk(-32, -32, 0));
            region.writeChunk(-1, -1, chunk(-1, -1, 0));
        }

        try (RegionFile region = RegionFile.open(file)) {
            assertTrue(region.hasChunk(-1, -1));
            assertEquals(-1, region.readChunk(-1, -1).getInt("xPos"));
            assertEquals(-32, region.readChunk(-32, -32).getInt("zPos"));
            assertFalse(region.hasChunk(-5, -5));
            assertNull(region.readChunk(-5, -5));
        }
    }

    @Test
    void growingChunksDoNotEatTheirNeighbours() throws IOException {
        File file = tempDir.resolve("r.0.0.mca").toFile();
        try (RegionFile region = RegionFile.create(file)) {
            region.writeChunk(0, 0, chunk(0, 0, 0));
            region.writeChunk(1, 0, chunk(1, 0, 0));
            // random bytes don't compress
            region.writeChunk(0, 0, chunk(0, 0, 20_000));
        }

        try (RegionFile region = RegionFile.open(file)) {
            assertEquals(20_000, region.readChunk(0, 0).getByteArray("noise").length);
            assertEquals(1, region.readChunk(1, 0).getInt("xPos"));
        }
    }

    @Test
    void refusesChunksTheFormatCannotHold() throws IOException {
        try (RegionFile region = RegionFile.create(tempDir.resolve("r.0.0.mca").toFile())) {
            assertThrows(IOException.class, () -> region.writeChunk(0, 0, chunk(0, 0, 1_100_000)));
        }
    }

    private static NbtCompound chunk(int x, int z, int noiseBytes) {
        byte[] noise = new byte[noiseBytes];
        new Random(42).nextBytes(noise);

        NbtCompound chunk = new NbtCompound();
        chunk.setInt("xPos", x);
        chunk.setInt("zPos", z);
        chunk.setByteArray("noise", noise);
        return chunk;
    }
}
