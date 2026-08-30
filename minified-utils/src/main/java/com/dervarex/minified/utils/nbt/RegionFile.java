package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Reads Anvil region files (.mca)
 * Format: 8192-byte header followed by
 * chunk payloads, each compressed individually
 */
public class RegionFile implements Closeable {

    private static final int SECTOR_SIZE = 4096;
    private static final int HEADER_SIZE = 8192;

    private static final int COMPRESSION_GZIP = 1;
    private static final int COMPRESSION_ZLIB = 2;
    private static final int COMPRESSION_NONE = 3;
    // 4 would be LZ4, not covered here, look package info for more details

    private final RandomAccessFile raf;
    private final int[] offsets = new int[1024]; // sector offset into file
    private final int[] sectorCounts = new int[1024];

    private RegionFile(RandomAccessFile raf) {
        this.raf = raf;
    }

    public static RegionFile open(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        RegionFile region = new RegionFile(raf);
        region.readHeader();
        return region;
    }

    private void readHeader() throws IOException {
        raf.seek(0);
        byte[] header = new byte[HEADER_SIZE];
        raf.readFully(header);

        for (int i = 0; i < 1024; i++) {
            int base = i * 4;
            int entry = ((header[base] & 0xFF) << 16)
                    | ((header[base + 1] & 0xFF) << 8)
                    | (header[base + 2] & 0xFF);
            int sectorCount = header[base + 3] & 0xFF;

            offsets[i] = entry;
            sectorCounts[i] = sectorCount;
        }
        // timestamps (bytes 4096..8191) currently ignored, getters not needed in minified-worlds, open an issue if you want me to add it
    }

    private static int chunkIndex(int chunkX, int chunkZ) {
        return (chunkX & 31) + (chunkZ & 31) * 32;
    }

    public boolean hasChunk(int chunkX, int chunkZ) {
        int idx = chunkIndex(chunkX, chunkZ);
        return offsets[idx] != 0 && sectorCounts[idx] != 0;
    }

    public NbtCompound readChunk(int chunkX, int chunkZ) throws IOException {
        int idx = chunkIndex(chunkX, chunkZ);
        if (!hasChunk(chunkX, chunkZ)) return null;

        long sectorOffset = offsets[idx] * (long) SECTOR_SIZE;

        raf.seek(sectorOffset);
        int length = raf.readInt();
        int compressionType = raf.readUnsignedByte();

        byte[] payload = new byte[length - 1];
        raf.readFully(payload);

        InputStream raw = new ByteArrayInputStream(payload);
        InputStream decompressed = switch (compressionType) {
            case COMPRESSION_GZIP -> new GZIPInputStream(raw);
            case COMPRESSION_ZLIB -> new InflaterInputStream(raw);
            case COMPRESSION_NONE -> raw;
            default -> throw new IOException(
                    "Unsupported compression type " + compressionType
                            + " (LZ4 is not supported!)");
        };

        try (DataInputStream in = new DataInputStream(decompressed)) {
            int rootType = in.readUnsignedByte();
            if (rootType != Parser.TAG_Compound) {
                throw new IOException("Chunk root tag isn't a compound (type " + rootType + ")");
            }
            in.readUTF(); // root name, empty for chunk data
            return Parser.readCompoundBody(in);
        }
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }
}
