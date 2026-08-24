package com.dervarex.minified.utils.nbt;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class Parser {

    static final int TAG_End = 0, TAG_Byte = 1, TAG_Short = 2, TAG_Int = 3,
            TAG_Long = 4, TAG_Float = 5, TAG_Double = 6, TAG_Byte_Array = 7,
            TAG_String = 8, TAG_List = 9, TAG_Compound = 10,
            TAG_Int_Array = 11, TAG_Long_Array = 12;

    /**
     * Parse the given .nbt file
     * @param file the file to parse, has to end with .nbt
     */
    public static LinkedHashMap<String, Object> readFile(File file) throws IOException {
        try (PushbackInputStream pb = new PushbackInputStream(new BufferedInputStream(
                new FileInputStream(file)), 2)) {

            byte[] header = new byte[2];
            int read = pb.read(header);
            if (read == 2) pb.unread(header);

            InputStream raw = getRaw(read, header, pb);
            try (DataInputStream in = new DataInputStream(raw)) {
                // root tag headers
                int rootType = in.readUnsignedByte();
                if (rootType != TAG_Compound) {
                    throw new IOException("Root tag isn't a compound (type " + rootType + ")");
                }
                in.readUTF(); // root name, for level.dat it's usually empty

                // Read compound recursive
                return readCompoundBody(in);
            }
        }
    }

    private static InputStream getRaw(int read, byte[] header, PushbackInputStream pb) throws IOException {
        InputStream raw;
        if (read == 2 && (header[0] & 0xFF) == 0x1F && (header[1] & 0xFF) == 0x8B) {
            raw = new GZIPInputStream(pb);                                                  // Gzip
        } else if (read == 2 && (header[0] & 0xFF) == 0x78) {
            raw = new InflaterInputStream(pb);                                              // Zlib
        } else {
            raw = pb;                                                                       // uncompressed
        }
        return raw;
    }

    /**
     * Reads the content from a compound, until TAG_END has been found
     */
    private static LinkedHashMap<String, Object> readCompoundBody(DataInputStream in) throws IOException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        while (true) {
            int type = in.readUnsignedByte();
            if (type == TAG_End) break;
            String name = in.readUTF();
            map.put(name, readPayload(type, in));
        }
        return map;
    }

    private static Object readPayload(int type, DataInputStream in) throws IOException {
        switch (type) {
            case TAG_Byte:   return in.readByte();
            case TAG_Short:  return in.readShort();
            case TAG_Int:    return in.readInt();
            case TAG_Long:   return in.readLong();
            case TAG_Float:  return in.readFloat();
            case TAG_Double: return in.readDouble();
            case TAG_String: return in.readUTF();
            case TAG_Byte_Array: {
                int len = in.readInt();
                byte[] arr = new byte[len];
                in.readFully(arr);
                return arr;
            }
            case TAG_Int_Array: {
                int len = in.readInt();
                int[] arr = new int[len];
                for (int i = 0; i < len; i++) arr[i] = in.readInt();
                return arr;
            }
            case TAG_Long_Array: {
                int len = in.readInt();
                long[] arr = new long[len];
                for (int i = 0; i < len; i++) arr[i] = in.readLong();
                return arr;
            }
            case TAG_List: {
                int elementType = in.readUnsignedByte();
                int len = in.readInt();
                List<Object> list = new ArrayList<>(Math.max(len, 0));
                for (int i = 0; i < len; i++) {
                    list.add(readPayload(elementType, in));
                }
                return list;
            }
            case TAG_Compound:
                return readCompoundBody(in);
            default:
                throw new IOException("Unknown Tag Type: " + type);
        }
    }
}
