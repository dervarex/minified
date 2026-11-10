package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apiguardian.api.API;

/**
 * The inventory of a player
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class Inventory {
    /**
     * Creates an empty inventory
     */
    public Inventory() {
    }

    /** The stacks */
    InventoryItem[] items;

    /**
     * Reads the inventory from NBT
     * @param nbt the player's Inventory list
     * @return the inventory
     */
    public static Inventory fromNbtList(NbtList nbt) {
        Inventory inventory = new Inventory();
        inventory.items = new InventoryItem[nbt.size()];
        for (int i = 0; i < nbt.size(); ++i) {
            NbtCompound entry = (NbtCompound) nbt.elements().get(i);
            InventoryItem item = new InventoryItem();
            item.raw = entry.copy();
            item.id = entry.getString("id");
            item.count = entry.has("count") ? entry.getInt("count") : 1; // no count means 1, says mojang
            item.slot = entry.getByte("Slot");
            inventory.items[i] = item;
        }
        return inventory;
    }

    /** {@return the inventory as NBT} */
    public NbtList toNbtList() {
        NbtList list = new NbtList((byte) 10); // 10 = compound tag id
        if (items == null) return list;
        for (InventoryItem item : items) {
            if (item == null) continue;
            NbtCompound entry = item.raw != null ? item.raw.copy() : new NbtCompound();
            entry.setString("id", item.id);
            entry.setInt("count", item.count);
            entry.setByte("Slot", item.slot);
            list.add(entry);
        }
        return list;
    }

    /**
     * One stack, its components (enchantments, names, ...) just come along for the ride
     */
    @Getter
    @Setter
    public static class InventoryItem {
        /**
         * Creates an empty item
         */
        public InventoryItem() {
        }

        /** The item id */
        String id;
        /** How many */
        int count;
        /** The slot */
        byte slot;
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        NbtCompound raw;
    }
}