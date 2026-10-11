package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Inventory {
    InventoryItem[] items;

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

    public NbtList toNbtList() {
        NbtList list = new NbtList((byte) 10); // 10 = compound tag id
        for (InventoryItem item : items) {
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
        String id;
        int count;
        byte slot;
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        NbtCompound raw;
    }
}