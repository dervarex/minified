package com.dervarex.minified.worlds.entity;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LivingEntityTest {

    @Test
    void testSetAndGetLivingStats() {
        NbtCompound raw = new NbtCompound();
        raw.setString("id", "minecraft:zombie");
        LivingEntity entity = new LivingEntity(raw);

        entity.setHealth(20.0f);
        entity.setLeftHanded(true);
        entity.setCanPickUpLoot(true);
        entity.setPersistenceRequired(true);
        entity.setAgeLocked(true);
        entity.setAge(-100);
        entity.setAbsorptionAmount(4.0f);

        assertEquals(20.0f, entity.health());
        assertTrue(entity.leftHanded());
        assertTrue(entity.canPickUpLoot());
        assertTrue(entity.persistenceRequired());
        assertTrue(entity.ageLocked());
        assertEquals(-100, entity.age());
        assertEquals(4.0f, entity.absorptionAmount());
    }

    @Test
    void testGetBrain() {
        NbtCompound raw = new NbtCompound();
        raw.setString("id", "minecraft:villager");
        NbtCompound brain = new NbtCompound();
        brain.setString("memories", "none");
        raw.setCompound("Brain", brain);

        LivingEntity entity = new LivingEntity(raw);

        assertSame(brain, entity.brain());
    }
}
