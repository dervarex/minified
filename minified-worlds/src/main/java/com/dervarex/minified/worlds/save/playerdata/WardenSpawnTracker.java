package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
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

    public static WardenSpawnTracker fromNbt(NbtCompound nbt) {
        WardenSpawnTracker tracker = new WardenSpawnTracker();
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
        NbtCompound nbt = new NbtCompound();
        if(cooldownTicks != null) {
            nbt.setInt("cooldown_ticks", cooldownTicks);
        }
        if(ticksSinceLastWarning != null) {
            nbt.setInt("ticks_since_last_warning", ticksSinceLastWarning);
        }
        if(warningLevel != null) {
            nbt.setInt("warning_level", warningLevel);
        }
        return nbt;
    }
}
