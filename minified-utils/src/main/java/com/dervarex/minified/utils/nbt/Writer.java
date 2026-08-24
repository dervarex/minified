package com.dervarex.minified.utils.nbt;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
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
    public static void writeFile(File file, LinkedHashMap<String, Object> nbt) throws IOException {
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
    public static void writeFileUncompressed(File file, LinkedHashMap<String, Object> nbt) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeByte(TAG_Compound);
            out.writeUTF("");
            writeCompoundBody(out, nbt);
        }
    }

    private static void writeCompoundBody(DataOutputStream out, LinkedHashMap<String, Object> map) throws IOException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            int type = typeOf(entry.getValue());
            out.writeByte(type);
            out.writeUTF(entry.getKey());
            writePayload(type, entry.getValue(), out);
        }
        out.writeByte(TAG_End);
    }

    @SuppressWarnings("unchecked")
    private static void writePayload(int type, Object value, DataOutputStream out) throws IOException {
        switch (type) {
            case TAG_Byte:   out.writeByte((Byte) value); break;
            case TAG_Short:  out.writeShort((Short) value); break;
            case TAG_Int:    out.writeInt((Integer) value); break;
            case TAG_Long:   out.writeLong((Long) value); break;
            case TAG_Float:  out.writeFloat((Float) value); break;
            case TAG_Double: out.writeDouble((Double) value); break;
            case TAG_String: out.writeUTF((String) value); break;
            case TAG_Byte_Array: {
                byte[] arr = (byte[]) value;
                out.writeInt(arr.length);
                out.write(arr);
                break;
            }
            case TAG_Int_Array: {
                int[] arr = (int[]) value;
                out.writeInt(arr.length);
                for (int v : arr) out.writeInt(v);
                break;
            }
            case TAG_Long_Array: {
                long[] arr = (long[]) value;
                out.writeInt(arr.length);
                for (long v : arr) out.writeLong(v);
                break;
            }
            case TAG_List: {
                List<Object> list = (List<Object>) value;
                int elementType = list.isEmpty() ? TAG_End : typeOf(list.getFirst());
                out.writeByte(elementType);
                out.writeInt(list.size());
                for (Object o : list) writePayload(elementType, o, out);
                break;
            }
            case TAG_Compound:
                writeCompoundBody(out, (LinkedHashMap<String, Object>) value);
                break;
            default:
                throw new IOException("Unknown Tag Type: " + type);
        }
    }

    /**
     * Maps a Java value back to its NBT tag type ID
     * Has to stay in sync with Parser.readPayload
     */
    private static int typeOf(Object value) {
        if (value instanceof Byte) return TAG_Byte;
        if (value instanceof Short) return TAG_Short;
        if (value instanceof Integer) return TAG_Int;
        if (value instanceof Long) return TAG_Long;
        if (value instanceof Float) return TAG_Float;
        if (value instanceof Double) return TAG_Double;
        if (value instanceof String) return TAG_String;
        if (value instanceof byte[]) return TAG_Byte_Array;
        if (value instanceof int[]) return TAG_Int_Array;
        if (value instanceof long[]) return TAG_Long_Array;
        if (value instanceof List) return TAG_List;
        if (value instanceof LinkedHashMap) return TAG_Compound;
        throw new IllegalArgumentException("Unsupported Java type for NBT: "
                + (value == null ? "null" : value.getClass()));
    }
}