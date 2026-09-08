package com.dervarex.minified.worlds.save.level;

import com.dervarex.minified.utils.nbt.tag.*;
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
    private float pitch;
    private float yaw;
    @Nullable
    private int[] pos;

    private static float getFloatValue(NbtTag tag) {
        if (tag instanceof NbtFloat) {
            return ((NbtFloat) tag).value();
        } else if (tag instanceof NbtInt) {
            return ((NbtInt) tag).value();
        } else if (tag instanceof NbtLong) {
            return ((NbtLong) tag).value();
        } else if (tag instanceof NbtDouble) {
            return (float) ((NbtDouble) tag).value();
        } else if (tag instanceof NbtByte) {
            return ((NbtByte) tag).value();
        } else if (tag instanceof NbtShort) {
            return ((NbtShort) tag).value();
        }
        return 0.0f;
    }

    public static Spawn fromNbt(NbtCompound spawnNbt) {
        Spawn spawn = new Spawn();

        if (spawnNbt.has("dimension")) {
            spawn.dimension = spawnNbt.getString("dimension");
        }

        if (spawnNbt.has("pitch")) {
            spawn.pitch = getFloatValue(spawnNbt.get("pitch").orElse(null));
        } else {
            spawn.pitch = 0.0f;
        }

        if (spawnNbt.has("yaw")) {
            spawn.yaw = getFloatValue(spawnNbt.get("yaw").orElse(null));
        } else {
            spawn.yaw = 0.0f;
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
        nbt.setInt("pitch", (int) pitch);
        nbt.setInt("yaw", (int) yaw);
        if (pos != null) {
            nbt.setIntArray("pos", pos);
        }
        return nbt;
    }
}