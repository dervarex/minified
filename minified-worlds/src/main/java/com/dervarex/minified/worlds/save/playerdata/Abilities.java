package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apiguardian.api.API;

/**
 * What a player is allowed to do, like flying or building
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class Abilities {
    /**
     * Creates the default abilities
     */
    public Abilities() {
    }

    /** The walking speed */
    private float walkSpeed = 0.1f;
    /** The flying speed */
    private float flySpeed = 0.05f;
    /** If the player can fly */
    private boolean mayfly;
    /** If the player flies right now */
    private boolean flying;
    /** If the player can't take damage */
    private boolean invulnerable;
    /** If the player can place and break blocks */
    private boolean mayBuild = true;
    /** If blocks break instantly */
    private boolean instabuild;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the abilities from NBT
     * @param nbt the player's abilities compound
     * @return the abilities
     */
    public static Abilities fromNbt(NbtCompound nbt) {
        Abilities abilities = new Abilities();
        abilities.raw = nbt.copy();
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

    /** {@return the abilities as NBT} */
    public NbtCompound toNbt() {
        NbtCompound nbt = raw != null ? raw.copy() : new NbtCompound();
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