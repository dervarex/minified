package com.dervarex.minified.worlds.block;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtInt;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlockStateTest {

    private static final BlockState LOG = new BlockState("minecraft:oak_log", Map.of("axis", "x"));

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

    @Test
    void readsEveryWayAPaletteStoresAState() {
        NbtCompound axis = new NbtCompound();
        axis.setString("axis", "x");
        NbtCompound current = new NbtCompound();
        current.setString("id", "minecraft:oak_log");
        current.setCompound("properties", axis);
        NbtCompound named = new NbtCompound();
        named.setString("Name", "minecraft:oak_log");
        named.setCompound("Properties", axis);
        NbtCompound wrapped = new NbtCompound();
        wrapped.setString("", "minecraft:stone");

        assertEquals(LOG, BlockState.fromNbt(current));
        assertEquals(LOG, BlockState.fromNbt(named));
        assertEquals(BlockState.of("minecraft:stone"), BlockState.fromNbt(new NbtString("minecraft:stone")));
        assertEquals(BlockState.of("minecraft:stone"), BlockState.fromNbt(wrapped));
    }

    @Test
    void refusesWhatIsNoBlockState() {
        NbtCompound nameless = new NbtCompound();
        nameless.setString("axis", "x");

        assertThrows(IllegalArgumentException.class, () -> BlockState.fromNbt(nameless));
        assertThrows(IllegalArgumentException.class, () -> BlockState.fromNbt(new NbtInt(1)));
    }

    @Test
    void writesIdsAndPropertiesSince5006() {
        assertEquals(BlockStateFormat.NAME_PROPERTIES, BlockStateFormat.forDataVersion(5005));
        assertEquals(BlockStateFormat.ID_PROPERTIES, BlockStateFormat.forDataVersion(5006));

        assertEquals(new NbtString("minecraft:stone"), BlockStateFormat.ID_PROPERTIES.toNbt(BlockState.of("minecraft:stone")));
        NbtCompound log = (NbtCompound) BlockStateFormat.ID_PROPERTIES.toNbt(LOG);
        assertEquals("minecraft:oak_log", log.getString("id"));
        assertEquals("x", log.getCompound("properties").getString("axis"));
    }

    @Test
    void writesNamesAndPropertiesBefore5006() {
        NbtCompound stone = (NbtCompound) BlockStateFormat.NAME_PROPERTIES.toNbt(BlockState.of("minecraft:stone"));
        assertEquals("minecraft:stone", stone.getString("Name"));
        assertFalse(stone.has("Properties"));

        NbtCompound log = (NbtCompound) BlockStateFormat.NAME_PROPERTIES.toNbt(LOG);
        assertEquals("minecraft:oak_log", log.getString("Name"));
        assertEquals("x", log.getCompound("Properties").getString("axis"));
    }
}
