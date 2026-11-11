package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.AccessLevel;
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
    /**
     * Creates empty wandering trader data
     */
    public WanderingTrader() {
    }

    /** The data version of the Minecraft version that wrote it */
    private Integer dataVersion;
    /** The chance of the next spawn, in percent */
    @Nullable
    private Integer spawnChance;
    /** The ticks until the next spawn try */
    @Nullable
    private Integer spawnDelay;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the wandering trader data from NBT
     * @param nbt the content of wandering_trader.dat
     * @return the wandering trader data
     */
    public static WanderingTrader fromNbt(NbtCompound nbt) {
        WanderingTrader result = new WanderingTrader();
        result.raw = nbt.copy();
        if (nbt.has("DataVersion")) {
            result.dataVersion = nbt.getInt("DataVersion");
        }

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
        NbtCompound root = RawNbt.copyOrNew(raw);
        NbtCompound data = RawNbt.child(root, "data");
        if (spawnChance != null) {
            data.setInt("spawn_chance", spawnChance);
        } else {
            data.remove("spawn_chance");
        }
        if (spawnDelay != null) {
            data.setInt("spawn_delay", spawnDelay);
        } else {
            data.remove("spawn_delay");
        }

        if (dataVersion != null) {
            root.setInt("DataVersion", dataVersion);
        } else {
            root.remove("DataVersion");
        }
        return root;
    }
}
