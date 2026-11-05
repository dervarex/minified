package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtTag;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

// for writing back into the NBT a file was read from
final class RawNbt {
    private RawNbt() {}

    static NbtCompound copyOrNew(NbtCompound raw) {
        return raw != null ? raw.copy() : new NbtCompound();
    }

    // getOrCreate basically
    static NbtCompound child(NbtCompound parent, String key) {
        if (parent.get(key).orElse(null) instanceof NbtCompound existing) return existing;
        NbtCompound created = new NbtCompound();
        parent.setCompound(key, created);
        return created;
    }

    // removes the entries we model that aren't in keep anymore
    static void removeMissing(NbtCompound compound, Set<String> keep, Predicate<NbtTag> modeled) {
        for (Map.Entry<String, NbtTag> entry : compound.asMap().entrySet()) {
            if (!keep.contains(entry.getKey()) && modeled.test(entry.getValue())) {
                compound.remove(entry.getKey());
            }
        }
    }
}
