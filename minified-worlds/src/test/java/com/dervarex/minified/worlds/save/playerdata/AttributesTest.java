package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributesTest {

    @Test
    void testLoadAttributes() {
        NbtCompound entry = new NbtCompound();
        entry.setString("id", "minecraft:generic.max_health");
        entry.setDouble("base", 20.0);
        NbtList list = new NbtList((byte) 10);
        list.add(entry);

        Attributes attributes = Attributes.fromNbtList(list);

        assertEquals(1, attributes.getAttributes().length);
        assertEquals("minecraft:generic.max_health", attributes.getAttributes()[0].id);
        assertEquals(20.0, attributes.getAttributes()[0].base);
    }

    @Test
    void testSaveAndLoadAttributes() {
        Attributes original = new Attributes();
        Attributes.Attribute attribute = new Attributes.Attribute();
        attribute.id = "minecraft:generic.movement_speed";
        attribute.base = 0.1;
        original.setAttributes(new Attributes.Attribute[]{attribute});

        Attributes parsed = Attributes.fromNbtList(original.toNbtList());

        assertEquals(1, parsed.getAttributes().length);
        assertEquals("minecraft:generic.movement_speed", parsed.getAttributes()[0].id);
        assertEquals(0.1, parsed.getAttributes()[0].base);
    }
}