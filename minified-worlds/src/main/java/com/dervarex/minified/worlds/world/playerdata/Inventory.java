package com.dervarex.minified.worlds.world.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
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
            item.id = entry.getString("id");
            item.count = entry.getByte("count");
            item.slot = entry.getByte("Slot");
            inventory.items[i] = item;
        }
        return inventory;
    }

    public NbtList toNbtList() {
        NbtList list = new NbtList((byte) 10); // 10 = compound tag id
        for (InventoryItem item : items) {
            NbtCompound entry = new NbtCompound();
            entry.setString("id", item.id);
            entry.setByte("count", item.count);
            entry.setByte("Slot", item.slot);
            list.add(entry);
        }
        return list;
    }

    public static class InventoryItem {
        String id;
        byte count;
        byte slot;
    }
}