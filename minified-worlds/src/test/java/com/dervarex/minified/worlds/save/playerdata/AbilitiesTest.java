package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbilitiesTest {

    @Test
    void testLoadEmptyData() {
        Abilities abilities = Abilities.fromNbt(new NbtCompound());

        assertEquals(0.1f, abilities.getWalkSpeed());
        assertEquals(0.05f, abilities.getFlySpeed());
        assertFalse(abilities.isMayfly());
        assertTrue(abilities.isMayBuild());
        assertFalse(abilities.isInstabuild());
    }

    @Test
    void testSaveAndLoadAbilities() {
        Abilities original = new Abilities();
        original.setWalkSpeed(0.2f);
        original.setFlySpeed(0.1f);
        original.setMayfly(true);
        original.setFlying(true);
        original.setInvulnerable(true);
        original.setMayBuild(false);
        original.setInstabuild(true);

        Abilities parsed = Abilities.fromNbt(original.toNbt());

        assertEquals(original.getWalkSpeed(), parsed.getWalkSpeed());
        assertEquals(original.getFlySpeed(), parsed.getFlySpeed());
        assertEquals(original.isMayfly(), parsed.isMayfly());
        assertEquals(original.isFlying(), parsed.isFlying());
        assertEquals(original.isInvulnerable(), parsed.isInvulnerable());
        assertEquals(original.isMayBuild(), parsed.isMayBuild());
        assertEquals(original.isInstabuild(), parsed.isInstabuild());
    }
}
