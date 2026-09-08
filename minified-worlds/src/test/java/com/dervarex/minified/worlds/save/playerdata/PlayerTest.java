package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private static NbtCompound minimalNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 3953);
        return nbt;
    }

    @Test
    void testLoadWithoutDataVersionFails() {
        assertThrows(NoSuchElementException.class, () -> Player.fromNbt(new NbtCompound(), null, null));
    }

    @Test
    void testLoadEmptyData() {
        Player player = Player.fromNbt(minimalNbt(), null, null);

        assertEquals(3953, player.getDataVersion());
        assertNull(player.getAdvancements());
        assertNull(player.getStatistics());
        assertNull(player.getAbilities());
        assertNull(player.getPosition());
        assertNull(player.getHealth());
    }

    @Test
    void testLoadPlayerWithJsonData() {
        JsonObject advJson = new JsonObject();
        advJson.put("DataVersion", 3953);
        JsonObject statsJson = new JsonObject();
        statsJson.put("DataVersion", 3953);

        Player player = Player.fromNbt(minimalNbt(), advJson, statsJson);

        assertNotNull(player.getAdvancements());
        assertNotNull(player.getStatistics());
    }

    @Test
    void testSaveAndLoadTransform() {
        Player original = new Player();
        original.setDataVersion(3953);
        original.setPosition(new ArrayList<>(List.of(1.0, 64.0, -1.0)));
        original.setMotion(new ArrayList<>(List.of(0.0, -0.078, 0.0)));
        original.setRotation(new ArrayList<>(List.of(90.0, 0.0)));

        Player parsed = Player.fromNbt(original.toNbt(), null, null);

        assertEquals(original.getPosition(), parsed.getPosition());
        assertEquals(original.getMotion(), parsed.getMotion());
        // rotation is stored as NbtFloat, so precision should be narrowed on the roundtrip
        assertEquals(90.0f, parsed.getRotation().get(0).floatValue());
        assertEquals(0.0f, parsed.getRotation().get(1).floatValue());
    }

    @Test
    void testSaveAndLoadUuid() {
        Player original = new Player();
        original.setDataVersion(1);
        original.setUUID(new ArrayList<>(List.of(1, 2, 3, 4)));

        Player parsed = Player.fromNbt(original.toNbt(), null, null);

        assertEquals(original.getUUID(), parsed.getUUID());
    }

    @Test
    void testSaveAndLoadNestedComponents() {
        Player original = new Player();
        original.setDataVersion(1);

        Abilities abilities = new Abilities();
        abilities.setFlying(true);
        original.setAbilities(abilities);

        RecipeBook recipeBook = new RecipeBook();
        recipeBook.setRecipes(new String[]{"minecraft:torch"});
        original.setRecipeBook(recipeBook);

        WardenSpawnTracker tracker = new WardenSpawnTracker();
        tracker.setWarningLevel(1);
        original.setWardenSpawnTracker(tracker);

        Attributes.Attribute attribute = new Attributes.Attribute();
        attribute.id = "minecraft:generic.max_health";
        attribute.base = 20.0;
        Attributes attributes = new Attributes();
        attributes.setAttributes(new Attributes.Attribute[]{attribute});
        original.setAttributes(attributes);

        Inventory.InventoryItem item = new Inventory.InventoryItem();
        item.id = "minecraft:stone";
        item.count = 1;
        item.slot = 0;
        Inventory inventory = new Inventory();
        inventory.setItems(new Inventory.InventoryItem[]{item});
        original.setInventory(inventory);

        Player parsed = Player.fromNbt(original.toNbt(), null, null);

        assertTrue(parsed.getAbilities().isFlying());
        assertArrayEquals(new String[]{"minecraft:torch"}, parsed.getRecipeBook().getRecipes());
        assertEquals(1, parsed.getWardenSpawnTracker().getWarningLevel());
        assertEquals("minecraft:generic.max_health", parsed.getAttributes().getAttributes()[0].id);
        assertEquals("minecraft:stone", parsed.getInventory().getItems()[0].id);
    }

    @Test
    void testSaveAndLoadPlayerStats() {
        Player original = new Player();
        original.setDataVersion(3953);
        original.setDimension("minecraft:overworld");
        original.setHealth(20.0f);
        original.setFallDistance(0.0);
        original.setFoodLevel(20);
        original.setInvulnerable(false);
        original.setSeenCredits(true);

        Player parsed = Player.fromNbt(original.toNbt(), null, null);

        assertEquals(original.getDimension(), parsed.getDimension());
        assertEquals(original.getHealth(), parsed.getHealth());
        assertEquals(original.getFallDistance(), parsed.getFallDistance());
        assertEquals(original.getFoodLevel(), parsed.getFoodLevel());
        assertEquals(original.getInvulnerable(), parsed.getInvulnerable());
        assertEquals(original.getSeenCredits(), parsed.getSeenCredits());
    }

    @Test
    void testSaveWithoutDataVersion() {
        Player player = new Player();
        assertFalse(player.toNbt().has("DataVersion"));
    }
}