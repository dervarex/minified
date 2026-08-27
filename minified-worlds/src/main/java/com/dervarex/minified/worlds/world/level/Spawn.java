package com.dervarex.minified.worlds.world.level;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtIntArray;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
public class Spawn {
    @Nullable
    private String dimension;
    private int pitch;
    private int yaw;
    @Nullable
    private int[] pos;

    public static Spawn fromNbt(NbtCompound spawnNbt) {
        Spawn spawn = new Spawn();
        if (spawnNbt.has("dimension")) {
            spawn.dimension = spawnNbt.getString("dimension");
        }
        if (spawnNbt.has("pitch")) {
            spawn.pitch = spawnNbt.getInt("pitch");
        }
        if (spawnNbt.has("yaw")) {
            spawn.yaw = spawnNbt.getInt("yaw");
        }
        if (spawnNbt.has("pos")) {
            spawnNbt.get("pos").ifPresent(tag -> {
                if (tag instanceof NbtIntArray intArray) {
                    spawn.pos = intArray.value();
                }
            });
        }
        return spawn;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        if (dimension != null) {
            nbt.setString("dimension", dimension);
        }
        nbt.setInt("pitch", pitch);
        nbt.setInt("yaw", yaw);
        if (pos != null) {
            nbt.setIntArray("pos", pos);
        }
        return nbt;
    }
}