package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.NbtEquals;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldGenSettingsTest {

    @Test
    void testLoadEmptyData() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 1);

        WorldGenSettings settings = WorldGenSettings.fromNbt(nbt);

        assertEquals(0, settings.getSeed());
        assertFalse(settings.isBonusChest());
        assertFalse(settings.isGenerateStructures());
        assertTrue(settings.getDimensions().isEmpty());
    }

    @Test
    void testSaveAndLoadBiomeSource() {
        WorldGenSettings.BiomeSource biomeSource = new WorldGenSettings.BiomeSource();
        biomeSource.setType("minecraft:multi_noise");
        biomeSource.setPreset("minecraft:overworld");

        WorldGenSettings.BiomeSource parsed = WorldGenSettings.BiomeSource.fromNbt(biomeSource.toNbt());

        assertEquals(biomeSource.getType(), parsed.getType());
        assertEquals(biomeSource.getPreset(), parsed.getPreset());
    }

    @Test
    void testSaveAndLoadGenerator() {
        WorldGenSettings.BiomeSource biomeSource = new WorldGenSettings.BiomeSource();
        biomeSource.setType("minecraft:multi_noise");

        WorldGenSettings.Generator generator = new WorldGenSettings.Generator();
        generator.setType("minecraft:noise");
        generator.setSettings("minecraft:overworld");
        generator.setBiomeSource(biomeSource);

        WorldGenSettings.Generator parsed = WorldGenSettings.Generator.fromNbt(generator.toNbt());

        assertEquals(generator.getType(), parsed.getType());
        assertEquals(generator.getSettings(), parsed.getSettings());
        assertEquals("minecraft:multi_noise", parsed.getBiomeSource().getType());
    }

    @Test
    void testLoadGeneratorWithoutBiomeSource() {
        NbtCompound nbt = new NbtCompound();
        nbt.setString("type", "minecraft:flat");

        WorldGenSettings.Generator generator = WorldGenSettings.Generator.fromNbt(nbt);

        assertNull(generator.getBiomeSource());
        assertFalse(generator.toNbt().has("biome_source"));
    }

    @Test
    void testSaveAndLoadDimension() {
        WorldGenSettings.Generator generator = new WorldGenSettings.Generator();
        generator.setType("minecraft:noise");

        WorldGenSettings.Dimension dimension = new WorldGenSettings.Dimension();
        dimension.setType("minecraft:overworld");
        dimension.setGenerator(generator);

        WorldGenSettings.Dimension parsed = WorldGenSettings.Dimension.fromNbt(dimension.toNbt());

        assertEquals(dimension.getType(), parsed.getType());
        assertEquals("minecraft:noise", parsed.getGenerator().getType());
    }

    @Test
    void testSaveAndLoadWorldGenSettings() {
        WorldGenSettings original = new WorldGenSettings();
        original.setDataVersion(3953);
        original.setSeed(42L);
        original.setBonusChest(true);
        original.setGenerateStructures(true);

        WorldGenSettings.Dimension dimension = new WorldGenSettings.Dimension();
        dimension.setType("minecraft:overworld");
        original.getDimensions().put("minecraft:overworld", dimension);

        WorldGenSettings parsed = WorldGenSettings.fromNbt(original.toNbt());

        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(original.getSeed(), parsed.getSeed());
        assertTrue(parsed.isBonusChest());
        assertTrue(parsed.isGenerateStructures());
        assertEquals(1, parsed.getDimensions().size());
        assertEquals("minecraft:overworld", parsed.getDimensions().get("minecraft:overworld").getType());
    }

    @Test
    void testSaveTwiceMatches() {
        WorldGenSettings original = new WorldGenSettings();
        original.setSeed(1L);
        NbtCompound first = original.toNbt();
        NbtCompound second = WorldGenSettings.fromNbt(first).toNbt();
        assertTrue(NbtEquals.deepEquals(first, second));
    }
}
