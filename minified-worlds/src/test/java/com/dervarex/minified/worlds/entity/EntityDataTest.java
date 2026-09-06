package com.dervarex.minified.worlds.entity;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityDataTest {

    private static NbtCompound rootWithEntities(NbtCompound... entities) {
        NbtCompound root = new NbtCompound();
        NbtList list = new NbtList((byte) 10);
        for (NbtCompound entity : entities) list.add(entity);
        root.setList("Entities", list);
        return root;
    }

    private static NbtCompound entity(String id) {
        NbtCompound raw = new NbtCompound();
        raw.setString("id", id);
        return raw;
    }

    private static NbtCompound livingEntity(String id) {
        NbtCompound raw = entity(id);
        raw.setFloat("Health", 10.0f);
        return raw;
    }

    @Test
    void testEmptyEntityList() {
        EntityData data = new EntityData(rootWithEntities());
        assertTrue(data.entities().isEmpty());
    }

    @Test
    void testEntityWithHealthIsLivingEntity() {
        EntityData data = new EntityData(rootWithEntities(livingEntity("minecraft:cow")));
        assertEquals(1, data.entities().size());
        assertInstanceOf(LivingEntity.class, data.entities().get(0));
    }

    @Test
    void testEntityWithoutHealthIsNormalEntity() {
        EntityData data = new EntityData(rootWithEntities(entity("minecraft:item")));
        assertEquals(1, data.entities().size());
        assertFalse(data.entities().get(0) instanceof LivingEntity);
    }

    @Test
    void testAddEntity() {
        EntityData data = new EntityData(rootWithEntities());

        Entity added = data.addEntity(livingEntity("minecraft:pig"));

        assertInstanceOf(LivingEntity.class, added);
        assertEquals(1, data.entities().size());
        assertEquals(1, data.raw().getList("Entities").size());
    }
}
