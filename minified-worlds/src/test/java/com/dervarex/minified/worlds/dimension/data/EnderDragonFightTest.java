package com.dervarex.minified.worlds.dimension.data;

import com.dervarex.minified.utils.nbt.NbtEquals;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtInt;
import com.dervarex.minified.utils.nbt.tag.NbtIntArray;
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
        assertArrayEquals(new int[]{1, 2, 3, 4}, data.getIntArray("dragon_uuid"));
        assertArrayEquals(new int[]{0, 64, 0}, data.getIntArray("exit_portal_location"));
        assertEquals(3, data.getList("gateways").size());
        assertTrue(data.getBoolean("dragon_killed"));
    }

    @Test
    void worksBeforeThereIsADragonOrPortal() {
        // a fresh 26.3 world, no dragon_uuid or exit_portal_location yet
        NbtList gateways = new NbtList((byte) 3);
        gateways.add(new NbtInt(7));
        NbtCompound data = new NbtCompound();
        data.setList("gateways", gateways);
        data.setByte("dragon_killed", (byte) 0);
        data.setInt("respawn_time", 0);
        data.setByte("needs_state_scanning", (byte) 1);
        data.setByte("previously_killed", (byte) 0);
        NbtCompound nbt = new NbtCompound();
        nbt.setCompound("data", data);
        nbt.setInt("DataVersion", 5023);

        EnderDragonFight fight = EnderDragonFight.fromNbt(nbt);

        assertNull(fight.getDragonUUID());
        assertNull(fight.getExitPortalLocation());
        assertTrue(fight.isNeedsStateScanning());
        assertTrue(NbtEquals.deepEquals(nbt, fight.toNbt()));
    }

    @Test
    void keepsARespawnThatIsGoingOn() {
        NbtCompound nbt = createMinimalNbt(new NbtList((byte) 3));
        NbtList crystals = new NbtList((byte) 11);
        crystals.add(new NbtIntArray(new int[]{1, 2, 3, 4}));
        nbt.getCompound("data").setString("respawn_stage", "summoning_pillars");
        nbt.getCompound("data").setList("respawn_crystals", crystals);

        EnderDragonFight fight = EnderDragonFight.fromNbt(nbt);
        fight.setDragonUUID(null);
        NbtCompound data = fight.toNbt().getCompound("data");

        assertEquals("summoning_pillars", data.getString("respawn_stage"));
        assertEquals(1, data.getList("respawn_crystals").size());
        assertFalse(data.has("dragon_uuid"));
    }
}
