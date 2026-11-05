package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.apiguardian.api.API;

/**
 * A boss bar made with /bossbar
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class BossEvent {
    /** the name, null if it isn't plain text */
    @Nullable
    private String name;
    private boolean visible;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads a boss bar from NBT
     * @param nbt the boss bar's NBT
     * @return the boss bar
     */
    public static BossEvent fromNbt(NbtCompound nbt) {
        BossEvent event = new BossEvent();
        event.raw = nbt.copy();
        event.name = RawNbt.stringOrNull(nbt, "Name");
        if (nbt.has("Visible")) {
            event.visible = nbt.getByte("Visible") != 0;
        }
        return event;
    }

    /** {@return the boss bar as NBT} */
    public NbtCompound toNbt() {
        NbtCompound compound = RawNbt.copyOrNew(raw);
        RawNbt.setStringOrKeep(compound, "Name", name);
        compound.setByte("Visible", (byte) (visible ? 1 : 0));
        return compound;
    }
}