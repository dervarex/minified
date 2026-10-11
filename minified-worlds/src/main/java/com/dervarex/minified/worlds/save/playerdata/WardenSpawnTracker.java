package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class WardenSpawnTracker {
    @Nullable
    private Integer cooldownTicks;
    @Nullable
    private Integer ticksSinceLastWarning;
    @Nullable
    private Integer warningLevel;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

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
