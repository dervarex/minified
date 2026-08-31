package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
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
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        RegionFile region = new RegionFile(raf);
        region.readHeader();
        return region;
    }

    /**
     * Creates a new, empty region file (a zeroed 8192-byte header, no chunks)
     * and opens it. Use this instead of open() if the .mca file doesn't exist yet,
     * e.g. the first time a chunk is written to a region that was not yet generated
     */
    public static RegionFile create(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(HEADER_SIZE);
        raf.write(new byte[HEADER_SIZE]);
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

    /**
     * Writes and serializes NBT data of the chunk into this region file,
     * at the specified sector position, for the provided chunk coordinates.
     * If the data is still able to fit in the chunk's previous sectors,
     * then these sectors are reused. Otherwise, sectors are appended to the end of the
     * file. Unused sectors, if the chunk is shrunk in size, remain in the file.
     * (Vanilla Minecraft behavior, defragmentation is not implemented.)
     * Data is always written with Zlib compression, even if the original chunk was
     * read with different compression. Updates header fields with location and timestamp.
     *
     * @throws IOException if the serialized+compressed chunk needs more than 255 sectors
     *                      (~1MB compressed), the region file format's own hard limit
     */
    public void writeChunk(int chunkX, int chunkZ, NbtCompound chunkData) throws IOException {
        int idx = chunkIndex(chunkX, chunkZ);

        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(rawBytes)) {
            out.writeByte(Parser.TAG_Compound);
            out.writeUTF(""); // root name, empty for chunk data
            NbtWriter.writeCompound(out, chunkData);
        }

        ByteArrayOutputStream compressedBytes = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressedBytes)) {
            deflater.write(rawBytes.toByteArray());
        }
        byte[] compressed = compressedBytes.toByteArray();

        int payloadLength = compressed.length + 1; // +1 for the compression-type byte
        int totalLength = 4 + payloadLength;        // + the length-int itself
        int neededSectors = (totalLength + SECTOR_SIZE - 1) / SECTOR_SIZE;

        if (neededSectors > 255) {
            throw new IOException("Chunk (" + chunkX + ", " + chunkZ + ") is too large to store: "
                    + neededSectors + " sectors needed, 255 is the format's maximum");
        }

        int sectorOffset;
        if (offsets[idx] != 0 && sectorCounts[idx] >= neededSectors) {
            sectorOffset = offsets[idx]; // fits into the existing allocation, reuse it
        } else {
            long appendAt = raf.length();
            sectorOffset = (int) Math.max(2, appendAt / SECTOR_SIZE); // never write into the header
        }

        long byteOffset = sectorOffset * (long) SECTOR_SIZE;
        raf.seek(byteOffset);
        raf.writeInt(payloadLength);
        raf.writeByte(COMPRESSION_ZLIB);
        raf.write(compressed);

        long written = 4 + payloadLength;
        long padded = (long) neededSectors * SECTOR_SIZE;
        long padding = padded - written;
        if (padding > 0) {
            raf.write(new byte[(int) padding]);
        }

        offsets[idx] = sectorOffset;
        sectorCounts[idx] = neededSectors;
        writeLocationEntry(idx, sectorOffset, neededSectors);
        writeTimestampEntry(idx, (int) (System.currentTimeMillis() / 1000));
    }

    private void writeLocationEntry(int idx, int sectorOffset, int sectorCount) throws IOException {
        raf.seek(idx * 4L);
        raf.writeByte((sectorOffset >> 16) & 0xFF);
        raf.writeByte((sectorOffset >> 8) & 0xFF);
        raf.writeByte(sectorOffset & 0xFF);
        raf.writeByte(sectorCount & 0xFF);
    }

    private void writeTimestampEntry(int idx, int unixTimestamp) throws IOException {
        raf.seek(SECTOR_SIZE + idx * 4L);
        raf.writeInt(unixTimestamp);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }
}