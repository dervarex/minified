package com.dervarex.minified.worlds.chunk;

import com.dervarex.minified.utils.nbt.tag.*;
import com.dervarex.minified.worlds.block.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChunkSection {
    private final byte sectionY;
    private final List<BlockState> palette;
    private final long[] data; // null if the whole section is just a single block type
    private final int bitsPerBlock;

    public ChunkSection(NbtCompound sectionTag) {
        this.sectionY = sectionTag.getByte("Y");

        NbtCompound blockStates = sectionTag.getCompound("block_states");
        NbtList paletteList = blockStates.getList("palette");

        this.palette = new ArrayList<>();
        for (NbtTag entry : paletteList.elements()) {
            NbtCompound entryCompound = (NbtCompound) entry;
            String name = entryCompound.getString("Name");
            Map<String, String> properties = new LinkedHashMap<>();
            if (entryCompound.has("Properties")) {
                NbtCompound props = entryCompound.getCompound("Properties");
                for (Map.Entry<String, NbtTag> e : props.asMap().entrySet()) {
                    if (e.getValue() instanceof NbtString s) {
                        properties.put(e.getKey(), s.value());
                    }
                }
            }
            palette.add(new BlockState(name, properties));
        }

        this.bitsPerBlock = Math.max(4, 32 - Integer.numberOfLeadingZeros(Math.max(1, palette.size() - 1)));
        this.data = blockStates.has("data") ? blockStates.getLongArray("data") : null;
    }

    public byte sectionY() { return sectionY; }

    private static int localIndex(int x, int y, int z) {
        return (y & 15) * 256 + (z & 15) * 16 + (x & 15);
    }

    public BlockState getBlock(int x, int y, int z) {
        if (palette.size() == 1) return palette.get(0);
        int paletteIndex = readPaletteIndex(localIndex(x, y, z));
        return palette.get(paletteIndex);
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        int paletteIndex = palette.indexOf(state);
        if (paletteIndex == -1) {
            throw new UnsupportedOperationException(
                    "Block state " + state.name() + " is not yet in this section's palette - " +
                            "palette, repacking is not implemented yet"); // todo: repacking to add blocks that weren't in the pallete
        }
        if (palette.size() == 1) return; // only one possible block, no need to write anything
        writePaletteIndex(localIndex(x, y, z), paletteIndex);
    }

    private int readPaletteIndex(int blockIndex) {
        long bitIndex = (long) blockIndex * bitsPerBlock;
        int longIndex = (int) (bitIndex / 64);
        int bitOffset = (int) (bitIndex % 64);

        long value = data[longIndex] >>> bitOffset;
        if (bitOffset + bitsPerBlock > 64) {
            int remaining = bitOffset + bitsPerBlock - 64;
            value |= data[longIndex + 1] << (bitsPerBlock - remaining);
        }
        return (int) (value & ((1L << bitsPerBlock) - 1));
    }

    private void writePaletteIndex(int blockIndex, int paletteIndex) {
        long bitIndex = (long) blockIndex * bitsPerBlock;
        int longIndex = (int) (bitIndex / 64);
        int bitOffset = (int) (bitIndex % 64);

        long mask = (1L << bitsPerBlock) - 1;
        long value = paletteIndex & mask;

        data[longIndex] &= ~(mask << bitOffset);
        data[longIndex] |= value << bitOffset;

        if (bitOffset + bitsPerBlock > 64) {
            int remaining = bitOffset + bitsPerBlock - 64;
            long remainingMask = (1L << remaining) - 1;
            data[longIndex + 1] &= ~remainingMask;
            data[longIndex + 1] |= value >>> (bitsPerBlock - remaining);
        }
    }
}