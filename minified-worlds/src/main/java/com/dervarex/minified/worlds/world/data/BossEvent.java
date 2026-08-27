package com.dervarex.minified.worlds.world.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BossEvent {
    private String name;
    private boolean visible;

    public static BossEvent fromNbt(NbtCompound nbt) {
        BossEvent event = new BossEvent();
        if (nbt.has("Name")) {
            event.name = nbt.getString("Name");
        }
        if (nbt.has("Visible")) {
            event.visible = nbt.getByte("Visible") != 0;
        }
        return event;
    }

    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        if (name != null) {
            compound.setString("Name", name);
        }
        compound.setByte("Visible", (byte) (visible ? 1 : 0));
        return compound;
    }
}