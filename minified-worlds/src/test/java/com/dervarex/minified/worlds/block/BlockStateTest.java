package com.dervarex.minified.worlds.block;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlockStateTest {

    @Test
    void createBlockWithoutProperties() {
        BlockState state = BlockState.of("minecraft:stone");
        assertEquals("minecraft:stone", state.name());
        assertTrue(state.properties().isEmpty());
    }

    @Test
    void checkIfBlocksAreSame() {
        BlockState a = new BlockState("minecraft:oak_stairs", Map.of("facing", "north"));
        BlockState b = new BlockState("minecraft:oak_stairs", Map.of("facing", "north"));
        BlockState c = new BlockState("minecraft:oak_stairs", Map.of("facing", "south"));

        assertEquals(a, b);
        assertNotEquals(a, c);
    }
}
