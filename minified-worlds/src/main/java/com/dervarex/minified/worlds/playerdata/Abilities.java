package com.dervarex.minified.worlds.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Abilities {
    private float walkSpeed = 0.1f;
    private float flySpeed = 0.05f;
    private boolean mayfly;
    private boolean flying;
    private boolean invulnerable;
    private boolean mayBuild = true;
    private boolean instabuild;

    public static Abilities fromNbt(NbtCompound nbt) {
        Abilities abilities = new Abilities();
        if (nbt.has("walkSpeed")) {
            abilities.walkSpeed = nbt.getFloat("walkSpeed");
        }
        if (nbt.has("flySpeed")) {
            abilities.flySpeed = nbt.getFloat("flySpeed");
        }
        if (nbt.has("mayfly")) {
            abilities.mayfly = nbt.getByte("mayfly") != 0;
        }
        if (nbt.has("flying")) {
            abilities.flying = nbt.getByte("flying") != 0;
        }
        if (nbt.has("invulnerable")) {
            abilities.invulnerable = nbt.getByte("invulnerable") != 0;
        }
        if (nbt.has("mayBuild")) {
            abilities.mayBuild = nbt.getByte("mayBuild") != 0;
        }
        if (nbt.has("instabuild")) {
            abilities.instabuild = nbt.getByte("instabuild") != 0;
        }
        return abilities;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.setFloat("walkSpeed", walkSpeed);
        nbt.setFloat("flySpeed", flySpeed);
        nbt.setByte("mayfly", (byte) (mayfly ? 1 : 0));
        nbt.setByte("flying", (byte) (flying ? 1 : 0));
        nbt.setByte("invulnerable", (byte) (invulnerable ? 1 : 0));
        nbt.setByte("mayBuild", (byte) (mayBuild ? 1 : 0));
        nbt.setByte("instabuild", (byte) (instabuild ? 1 : 0));
        return nbt;
    }
}