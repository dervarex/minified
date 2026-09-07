package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRulesTest {

    @Test
    void testLoadBooleanAndIntegerRules() {
        NbtCompound data = new NbtCompound();
        data.setByte("doDaylightCycle", (byte) 1);
        data.setInt("randomTickSpeed", 3);
        NbtCompound nbt = new NbtCompound();
        nbt.setCompound("data", data);
        nbt.setInt("DataVersion", 3953);

        GameRules rules = GameRules.fromNbt(nbt);

        assertEquals(3953, rules.getDataVersion());
        assertEquals(Boolean.TRUE, rules.getBooleanGamerules().get("doDaylightCycle"));
        assertEquals(Integer.valueOf(3), rules.getIntegerGamerules().get("randomTickSpeed"));
    }

    @Test
    void testLoadEmptyRules() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 1);

        GameRules rules = GameRules.fromNbt(nbt);

        assertTrue(rules.getBooleanGamerules().isEmpty());
        assertTrue(rules.getIntegerGamerules().isEmpty());
    }

    @Test
    void testAddAndRemoveRules() {
        GameRules rules = new GameRules();
        rules.putBooleanGamerule("keepInventory", true);
        rules.putIntegerGamerule("randomTickSpeed", 5);

        assertTrue(rules.getBooleanGamerules().get("keepInventory"));
        assertEquals(5, rules.getIntegerGamerules().get("randomTickSpeed"));

        rules.removeBooleanGamerule("keepInventory");
        rules.removeIntegerGamerule("randomTickSpeed");

        assertTrue(rules.getBooleanGamerules().isEmpty());
        assertTrue(rules.getIntegerGamerules().isEmpty());
    }

    @Test
    void testSaveAndLoadGameRules() {
        GameRules original = new GameRules();
        original.setDataVersion(3953);
        original.putBooleanGamerule("doFireTick", false);
        original.putIntegerGamerule("maxEntityCramming", 24);

        GameRules parsed = GameRules.fromNbt(original.toNbt());

        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(Boolean.FALSE, parsed.getBooleanGamerules().get("doFireTick"));
        assertEquals(Integer.valueOf(24), parsed.getIntegerGamerules().get("maxEntityCramming"));
    }
}
