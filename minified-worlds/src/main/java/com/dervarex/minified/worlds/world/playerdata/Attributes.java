package com.dervarex.minified.worlds.world.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;

public class Attributes {
    Attribute[] attributes;

    public static Attributes fromNbtList(NbtList nbt) {
        Attributes attributesNbt = new Attributes();
        attributesNbt.attributes = new Attribute[nbt.size()];
        for (int i = 0; i < nbt.size(); ++i) {
            NbtCompound entry = (NbtCompound) nbt.elements().get(i);
            Attribute attribute = new Attribute();
            attribute.id = entry.getString("id");
            attribute.base = entry.getDouble("base");
            attributesNbt.attributes[i] = attribute;
        }
        return attributesNbt;
    }

    public NbtList toNbtList() {
        NbtList list = new NbtList((byte) 10); // 10 = compound tag id
        for (Attribute attribute : attributes) {
            NbtCompound entry = new NbtCompound();
            entry.setString("id", attribute.id);
            entry.setDouble("base", attribute.base);
            list.add(entry);
        }
        return list;
    }

    public static class Attribute {
        String id;
        double base;
    }
}