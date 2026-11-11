package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apiguardian.api.API;

/**
 * The attributes of a player, like max health or speed
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class Attributes {
    /**
     * Creates empty attributes
     */
    public Attributes() {
    }

    /** The attributes */
    Attribute[] attributes;

    /**
     * Reads the attributes from NBT
     * @param nbt the player's attributes list
     * @return the attributes
     */
    public static Attributes fromNbtList(NbtList nbt) {
        Attributes attributesNbt = new Attributes();
        attributesNbt.attributes = new Attribute[nbt.size()];
        for (int i = 0; i < nbt.size(); ++i) {
            NbtCompound entry = (NbtCompound) nbt.elements().get(i);
            Attribute attribute = new Attribute();
            attribute.raw = entry.copy();
            attribute.id = entry.getString("id");
            attribute.base = entry.getDouble("base");
            attributesNbt.attributes[i] = attribute;
        }
        return attributesNbt;
    }

    /** {@return the attributes as NBT} */
    public NbtList toNbtList() {
        NbtList list = new NbtList((byte) 10); // 10 = compound tag id
        if (attributes == null) return list;
        for (Attribute attribute : attributes) {
            if (attribute == null) continue;
            NbtCompound entry = attribute.raw != null ? attribute.raw.copy() : new NbtCompound();
            entry.setString("id", attribute.id);
            entry.setDouble("base", attribute.base);
            list.add(entry);
        }
        return list;
    }

    /**
     * An attribute's base value
     */
    @Getter
    @Setter
    public static class Attribute {
        /**
         * Creates an empty attribute
         */
        public Attribute() {
        }

        /** The attribute id */
        String id;
        /** The base value */
        double base;
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        NbtCompound raw;
    }
}