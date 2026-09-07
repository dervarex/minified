package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.*;

import java.io.*;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class Writer {

    static final int TAG_End = 0, TAG_Byte = 1, TAG_Short = 2, TAG_Int = 3,
            TAG_Long = 4, TAG_Float = 5, TAG_Double = 6, TAG_Byte_Array = 7,
            TAG_String = 8, TAG_List = 9, TAG_Compound = 10,
            TAG_Int_Array = 11, TAG_Long_Array = 12;

    /**
     * Writes the given NBT tree to a file, Gzip compressed (like the original Minecraft nbt files)
     * @param file target file
     * @param nbt root compound, e.g. as returned by Parser.readFile
     */
    public static void writeFile(File file, NbtCompound nbt) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))))) {
            out.writeByte(TAG_Compound);
            out.writeUTF(""); // root name, empty for level.dat
            writeCompoundBody(out, nbt);
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
            out.writeByte(TAG_Compound);
            out.writeUTF("");
            writeCompoundBody(out, nbt);
        }
    }

    private static void writeCompoundBody(DataOutputStream out, NbtCompound compound) throws IOException {
        for (Map.Entry<String, NbtTag> entry : compound.asMap().entrySet()) {
            NbtTag value = entry.getValue();
            out.writeByte(value.id());
            out.writeUTF(entry.getKey());
            writePayload(value, out);
        }
        out.writeByte(TAG_End);
    }

    private static void writePayload(NbtTag tag, DataOutputStream out) throws IOException {
        switch (tag) {
            case NbtByte t -> out.writeByte(t.value());
            case NbtBoolean t -> out.writeByte(t.value() ? 1 : 0);
            case NbtShort t -> out.writeShort(t.value());
            case NbtInt t -> out.writeInt(t.value());
            case NbtLong t -> out.writeLong(t.value());
            case NbtFloat t -> out.writeFloat(t.value());
            case NbtDouble t -> out.writeDouble(t.value());
            case NbtString t -> out.writeUTF(t.value());
            case NbtByteArray t -> {
                out.writeInt(t.value().length);
                out.write(t.value());
            }
            case NbtIntArray t -> {
                out.writeInt(t.value().length);
                for (int v : t.value()) out.writeInt(v);
            }
            case NbtLongArray t -> {
                out.writeInt(t.value().length);
                for (long v : t.value()) out.writeLong(v);
            }
            case NbtList t -> {
                out.writeByte(t.elementId());
                out.writeInt(t.size());
                for (NbtTag element : t.elements()) writePayload(element, out);
            }
            case NbtCompound t -> writeCompoundBody(out, t);
            case NbtEnd t -> throw new IOException("Cannot write a TAG_End as a value");
        }
    }
}