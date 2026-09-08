package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void testLoadInventory() {
        NbtCompound entry = new NbtCompound();
        entry.setString("id", "minecraft:diamond_sword");
        entry.setByte("count", (byte) 1);
        entry.setByte("Slot", (byte) 0);
        NbtList list = new NbtList((byte) 10);
        list.add(entry);

        Inventory inventory = Inventory.fromNbtList(list);

        assertEquals(1, inventory.getItems().length);
        assertEquals("minecraft:diamond_sword", inventory.getItems()[0].id);
        assertEquals((byte) 1, inventory.getItems()[0].count);
        assertEquals((byte) 0, inventory.getItems()[0].slot);
    }

    @Test
    void testSaveAndLoadInventory() {
        Inventory original = new Inventory();
        Inventory.InventoryItem item = new Inventory.InventoryItem();
        item.id = "minecraft:stone";
        item.count = 64;
        item.slot = 9;
        original.setItems(new Inventory.InventoryItem[]{item});

        Inventory parsed = Inventory.fromNbtList(original.toNbtList());

        assertEquals(1, parsed.getItems().length);
        assertEquals("minecraft:stone", parsed.getItems()[0].id);
        assertEquals((byte) 64, parsed.getItems()[0].count);
        assertEquals((byte) 9, parsed.getItems()[0].slot);
    }
}