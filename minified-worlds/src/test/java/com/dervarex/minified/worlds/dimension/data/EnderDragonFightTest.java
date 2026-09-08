package com.dervarex.minified.worlds.dimension.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtInt;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnderDragonFightTest {

    private static NbtCompound createMinimalNbt(NbtList gateways) {
        NbtCompound data = new NbtCompound();
        data.setBoolean("previously_killed", true);
        data.setBoolean("needs_state_scanning", false);
        data.setInt("respawn_time", 0);
        data.setBoolean("dragon_killed", true);
        data.setIntArray("dragon_uuid", new int[]{1, 2, 3, 4});
        data.setIntArray("exit_portal_location", new int[]{0, 64, 0});
        data.setList("gateways", gateways);

        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 3953);
        nbt.setCompound("data", data);
        return nbt;
    }

    @Test
    void testLoadBasicData() {
        EnderDragonFight fight = EnderDragonFight.fromNbt(createMinimalNbt(new NbtList((byte) 3)));

        assertEquals(3953, fight.getDataVersion());
        assertTrue(fight.isPreviouslyKilled());
        assertFalse(fight.isNeedsStateScanning());
        assertTrue(fight.isDragonKilled());
    }

    @Test
    void testLoadArrays() {
        EnderDragonFight fight = EnderDragonFight.fromNbt(createMinimalNbt(new NbtList((byte) 3)));

        assertArrayEquals(new int[]{1, 2, 3, 4}, fight.getDragonUUID());
        assertArrayEquals(new int[]{0, 64, 0}, fight.getExitPortalLocation());
    }

    @Test
    void fromNbtParsesNonEmptyGateways() {
        NbtList gateways = new NbtList((byte) 3);
        gateways.add(new NbtInt(1));
        gateways.add(new NbtInt(2));

        EnderDragonFight fight = EnderDragonFight.fromNbt(createMinimalNbt(gateways));

        assertArrayEquals(new int[]{1, 2}, fight.getGateways());
    }

    @Test
    void toNbtWritesAllFields() {
        EnderDragonFight fight = new EnderDragonFight();
        fight.setDataVersion(3953);
        fight.setDragonUUID(new int[]{1, 2, 3, 4});
        fight.setExitPortalLocation(new int[]{0, 64, 0});
        fight.setGateways(new int[]{1, 2, 3});
        fight.setDragonKilled(true);

        NbtCompound nbt = fight.toNbt();
        NbtCompound data = nbt.getCompound("data");

        assertEquals(3953, nbt.getInt("DataVersion"));
        assertArrayEquals(new int[]{1, 2, 3, 4}, data.getIntArray("dragon_uuid").value());
        assertArrayEquals(new int[]{0, 64, 0}, data.getIntArray("exit_portal_location").value());
        assertEquals(3, data.getList("gateways").size());
        assertTrue(data.getBoolean("dragon_killed"));
    }
}
