package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class Writer {

    /**
     * Writes the given NBT tree to a file, Gzip compressed (like the original Minecraft nbt files)
     * @param file target file
     * @param nbt root compound, e.g. as returned by Parser.readFile
     */
    public static void writeFile(File file, NbtCompound nbt) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))))) {
            out.writeByte(Parser.TAG_Compound);
            out.writeUTF(""); // root name, empty for level.dat
            NbtWriter.writeCompound(out, nbt);
        }
    }

    /**
     * Writes the given NBT tree uncompressed. Useful for debugging or
     * formats that don't expect Gzip (e.g. individual chunk payloads
     * that are compressed at the region-file level instead).
     */
    public static void writeFileUncompressed(File file, NbtCompound nbt) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeByte(Parser.TAG_Compound);
            out.writeUTF("");
            NbtWriter.writeCompound(out, nbt);
        }
    }
}
