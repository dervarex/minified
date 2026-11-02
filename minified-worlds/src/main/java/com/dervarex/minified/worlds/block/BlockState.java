package com.dervarex.minified.worlds.block;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import com.dervarex.minified.utils.nbt.tag.NbtTag;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apiguardian.api.API;

/**
 * A block with its properties, e.g. minecraft:oak_log with axis=x.
 * No properties means the block's default state, since 5006 Minecraft doesn't even store them for that one
 */
@API(status = API.Status.STABLE)
public record BlockState(String name, Map<String, String> properties) {
    public static BlockState of(String name) {
        return new BlockState(name, Map.of());
    }

    /**
     * Reads a palette entry
     * @throws IllegalArgumentException if it's no block state at all
     */
    public static BlockState fromNbt(NbtTag tag) {
        if (tag instanceof NbtString id) return of(id.value());
        if (tag instanceof NbtCompound entry) {
            // {"": "minecraft:stone"}, mojang's way of putting a string into a list of compounds. sure, why not
            if (entry.asMap().size() == 1 && entry.get("").orElse(null) instanceof NbtString id) return of(id.value());

            boolean named = !entry.has("id");
            if (entry.get(named ? "Name" : "id").orElse(null) instanceof NbtString id) {
                Map<String, String> properties = new LinkedHashMap<>();
                if (entry.get(named ? "Properties" : "properties").orElse(null) instanceof NbtCompound values) {
                    for (Map.Entry<String, NbtTag> value : values.asMap().entrySet()) {
                        if (value.getValue() instanceof NbtString string) properties.put(value.getKey(), string.value());
                    }
                }
                return new BlockState(id.value(), properties);
            }
            throw new IllegalArgumentException("Not a block state, a compound without id or Name: " + entry.asMap().keySet());
        }
        throw new IllegalArgumentException("Not a block state: " + tag);
    }
}