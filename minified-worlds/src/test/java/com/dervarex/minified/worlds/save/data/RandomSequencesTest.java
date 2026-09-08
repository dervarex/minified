package com.dervarex.minified.worlds.save.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomSequencesTest {

    @Test
    void testLoadEmptyData() {
        com.dervarex.minified.utils.nbt.tag.NbtCompound nbt = new com.dervarex.minified.utils.nbt.tag.NbtCompound();
        nbt.setInt("DataVersion", 1);
        RandomSequences result = RandomSequences.fromNbt(nbt);
        assertEquals(0, result.getSalt());
        assertTrue(result.getSequences().isEmpty());
    }

    @Test
    void testAddAndRemoveSequence() {
        RandomSequences sequences = new RandomSequences();
        sequences.putSequence("minecraft:trial_key", new long[]{1, 2, 3});

        assertArrayEquals(new long[]{1, 2, 3}, sequences.getSequences().get("minecraft:trial_key"));

        sequences.removeSequence("minecraft:trial_key");
        assertTrue(sequences.getSequences().isEmpty());
    }

    @Test
    void testSaveAndLoadRandomSequences() {
        RandomSequences original = new RandomSequences();
        original.setDataVersion(3953);
        original.setSalt(42);
        original.putSequence("minecraft:trial_key", new long[]{5, 6, 7});

        RandomSequences parsed = RandomSequences.fromNbt(original.toNbt());

        assertEquals(original.getDataVersion(), parsed.getDataVersion());
        assertEquals(original.getSalt(), parsed.getSalt());
        assertArrayEquals(original.getSequences().get("minecraft:trial_key"),
                parsed.getSequences().get("minecraft:trial_key"));
    }
}
