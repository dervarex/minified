package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.apiguardian.api.API;

/**
 * When the next wandering trader spawns, from wandering_trader.dat
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class WanderingTrader {
    private Integer dataVersion;
    @Nullable
    private Integer spawnChance;
    @Nullable
    private Integer spawnDelay;

    /**
     * Reads the wandering trader data from NBT
     * @param nbt the content of wandering_trader.dat
     * @return the wandering trader data
     */
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

    /** {@return the wandering trader data as NBT, like in wandering_trader.dat} */
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
