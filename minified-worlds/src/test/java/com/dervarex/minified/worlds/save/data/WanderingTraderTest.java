package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WanderingTraderTest {

    @Test
    void testLoadEmptyData() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 1);

        WanderingTrader trader = WanderingTrader.fromNbt(nbt);

        assertNull(trader.getSpawnChance());
        assertNull(trader.getSpawnDelay());
    }

    @Test
    void testSaveWithoutOptionalFields() {
        WanderingTrader trader = new WanderingTrader();
        trader.setDataVersion(1);

        NbtCompound nbt = trader.toNbt();
        NbtCompound data = nbt.getCompound("data");

        assertFalse(data.has("spawn_chance"));
        assertFalse(data.has("spawn_delay"));
    }

    @Test
    void testSaveAndLoadWanderingTrader() {
        WanderingTrader original = new WanderingTrader();
        original.setDataVersion(3953);
        original.setSpawnChance(25);
        original.setSpawnDelay(12000);

        WanderingTrader parsed = WanderingTrader.fromNbt(original.toNbt());

        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(original.getSpawnChance(), parsed.getSpawnChance());
        assertEquals(original.getSpawnDelay(), parsed.getSpawnDelay());
    }
}
