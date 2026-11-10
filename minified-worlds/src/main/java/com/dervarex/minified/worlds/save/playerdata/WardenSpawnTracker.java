package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.apiguardian.api.API;

/**
 * How close a player is to summoning a warden
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class WardenSpawnTracker {
    /**
     * Creates an empty warden spawn tracker
     */
    public WardenSpawnTracker() {
    }

    @Nullable
    private Integer cooldownTicks;
    @Nullable
    private Integer ticksSinceLastWarning;
    @Nullable
    private Integer warningLevel;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the tracker from NBT
     * @param nbt the player's warden_spawn_tracker compound
     * @return the tracker
     */
    public static WardenSpawnTracker fromNbt(NbtCompound nbt) {
        WardenSpawnTracker tracker = new WardenSpawnTracker();
        tracker.raw = nbt.copy();
        if(nbt.has("cooldown_ticks")) {
            tracker.cooldownTicks = nbt.getInt("cooldown_ticks");
        }
        if(nbt.has("ticks_since_last_warning")) {
            tracker.ticksSinceLastWarning = nbt.getInt("ticks_since_last_warning");
        }
        if(nbt.has("warning_level")) {
            tracker.warningLevel = nbt.getInt("warning_level");
        }
        return tracker;
    }

    /** {@return the tracker as NBT} */
    public NbtCompound toNbt() {
        NbtCompound nbt = raw != null ? raw.copy() : new NbtCompound();
        if(cooldownTicks != null) {
            nbt.setInt("cooldown_ticks", cooldownTicks);
        } else {
            nbt.remove("cooldown_ticks");
        }
        if(ticksSinceLastWarning != null) {
            nbt.setInt("ticks_since_last_warning", ticksSinceLastWarning);
        } else {
            nbt.remove("ticks_since_last_warning");
        }
        if(warningLevel != null) {
            nbt.setInt("warning_level", warningLevel);
        } else {
            nbt.remove("warning_level");
        }
        return nbt;
    }
}
