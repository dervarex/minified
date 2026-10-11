package com.dervarex.minified.worlds.block;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import com.dervarex.minified.utils.nbt.tag.NbtTag;

import java.util.Map;

/**
 * How a chunk's palettes store block states. Mojang changed it in DataVersion 5006, because why not
 */
public enum BlockStateFormat {
    /**
     * {@code {Name: "minecraft:oak_log", Properties: {axis: "x"}}}, before 5006
     */
    NAME_PROPERTIES,
    /**
     * {@code "minecraft:oak_log"} for default states, {@code {id: "minecraft:oak_log", properties: {axis: "x"}}} for the rest.
     * Name is id now and Properties is properties, damn mojang, was that really necessary?
     */
    ID_PROPERTIES;

    /**
     * The first DataVersion that uses {@link #ID_PROPERTIES}
     */
    public static final int ID_PROPERTIES_SINCE = 5006;

    public static BlockStateFormat forDataVersion(int dataVersion) {
        return dataVersion >= ID_PROPERTIES_SINCE ? ID_PROPERTIES : NAME_PROPERTIES;
    }

    /**
     * @return an {@link NbtString} or an {@link NbtCompound}
     */
    public NbtTag toNbt(BlockState state) {
        if (this == ID_PROPERTIES && state.properties().isEmpty()) return new NbtString(state.name());

        NbtCompound entry = new NbtCompound();
        entry.setString(this == ID_PROPERTIES ? "id" : "Name", state.name());
        if (!state.properties().isEmpty()) {
            NbtCompound properties = new NbtCompound();
            for (Map.Entry<String, String> property : state.properties().entrySet()) {
                properties.setString(property.getKey(), property.getValue());
            }
            entry.setCompound(this == ID_PROPERTIES ? "properties" : "Properties", properties);
        }
        return entry;
    }
}
