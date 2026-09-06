package com.dervarex.minified.worlds.entity;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    private static NbtCompound base(String id) {
        NbtCompound raw = new NbtCompound();
        raw.setString("id", id);
        return raw;
    }

    @Test
    void testReadId() {
        Entity entity = new Entity(base("minecraft:cow"));
        assertEquals("minecraft:cow", entity.id());
    }

    @Test
    void testSetAndGetUuid() {
        Entity entity = new Entity(base("minecraft:cow"));
        UUID uuid = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");

        entity.setUuid(uuid);

        assertEquals(uuid, entity.uuid());
    }

    @Test
    void testSetAndGetPosAndMotion() {
        Entity entity = new Entity(base("minecraft:cow"));

        entity.setPos(1.5, 64.0, -2.5);
        entity.setMotion(0.0, -0.08, 0.0);

        assertArrayEquals(new double[]{1.5, 64.0, -2.5}, entity.pos());
        assertArrayEquals(new double[]{0.0, -0.08, 0.0}, entity.motion());
    }

    @Test
    void testSetAndGetRotation() {
        Entity entity = new Entity(base("minecraft:cow"));

        entity.setRotation(90.0f, 45.0f);

        assertArrayEquals(new float[]{90.0f, 45.0f}, entity.rotation());
    }

    @Test
    void testSetAndGetBasicStats() {
        Entity entity = new Entity(base("minecraft:cow"));

        entity.setOnGround(true);
        entity.setInvulnerable(true);
        entity.setFallDistance(2.5);
        entity.setAir((short) 300);
        entity.setFire((short) 5);
        entity.setPortalCooldown(10);

        assertTrue(entity.onGround());
        assertTrue(entity.invulnerable());
        assertEquals(2.5, entity.fallDistance());
        assertEquals((short) 300, entity.air());
        assertEquals((short) 5, entity.fire());
        assertEquals(10, entity.portalCooldown());
    }
}
