package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class WanderingTrader {
    private Integer dataVersion;
    @Nullable
    private Integer spawnChance;
    @Nullable
    private Integer spawnDelay;

    public static WanderingTrader fromNbt(NbtCompound nbt) {
        WanderingTrader result = new WanderingTrader();
        result.dataVersion = nbt.getInt("DataVersion");

        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");
            if(data.has("spawn_chance")) {
                result.spawnChance = data.getInt("spawn_chance");
            }
            if(data.has("spawn_delay")) {
                result.spawnDelay = data.getInt("spawn_delay");
            }
        }
        return result;
    }

    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();
        if (spawnChance != null) {
            data.setInt("spawn_chance", spawnChance);
        }
        if (spawnDelay != null) {
            data.setInt("spawn_delay", spawnDelay);
        }

        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }
}
