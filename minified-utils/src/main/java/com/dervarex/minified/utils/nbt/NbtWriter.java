package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class NbtWriter {

    /**
     * Writes a compounds entries followed by TAG_End
     */
    public static void writeCompound(DataOutputStream out, NbtCompound compound) throws IOException {
        for (Map.Entry<String, NbtTag> entry : compound.asMap().entrySet()) {
            NbtTag tag = entry.getValue();
            out.writeByte(tag.id());
            out.writeUTF(entry.getKey());
            writePayload(tag, out);
        }
        out.writeByte(Parser.TAG_End);
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
            case NbtByteArray t -> {
                out.writeInt(t.value().length);
                out.write(t.value());
            }
            case NbtString t -> out.writeUTF(t.value());
            case NbtList t -> {
                out.writeByte(t.elementId());
                out.writeInt(t.size());
                for (NbtTag element : t.elements()) writePayload(element, out);
            }
            case NbtCompound t -> writeCompound(out, t);
            case NbtIntArray t -> {
                out.writeInt(t.value().length);
                for (int v : t.value()) out.writeInt(v);
            }
            case NbtLongArray t -> {
                out.writeInt(t.value().length);
                for (long v : t.value()) out.writeLong(v);
            }
            case NbtEnd ignored -> { /* never occurs as a value */ }
        }
    }
}