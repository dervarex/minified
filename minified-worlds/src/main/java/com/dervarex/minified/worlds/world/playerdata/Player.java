package com.dervarex.minified.worlds.world.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

import java.util.ArrayList;
import java.util.UUID;

// a31ccf30-00e4-4928-a590-e366c90af710.dat (<player-uuid>.dat)

public class Player {

    UUID uuid;

    Advancements advancements;

    Stats statistics;

    Abilities abilities;

    RecipeBook recipeBook;

    Short hurtTime;
    ArrayList<Double> motion; // e.g. 0, -0.0784000015258789, 0
    ArrayList<Double> position; // e.g. 360.1760070593703, 77, 195.91003479668723
    ArrayList<Double> rotation; // e.g. 155.59056, 49.508533

    Attributes attributes;

    ArrayList<Integer> UUID; // 4 integers -> UUID (most/least significant bits split in halves)

    String dimension;
    Double fallDistance;
    Float absorptionAmount;
    Float health;
    Float XpP; // percent needed to reach the next xp level

    Inventory inventory;

    Float foodSaturationLevel;
    int currentImpulseContextResetGraceTime;
    int hurtByTimestamp;
    int portalCooldown;
    int score;
    int selectedItemSlot;
    int XPLevel;
    int XPSeed;
    int XPTotal;

    WardenSpawnTracker wardenSpawnTracker;

    int foodLevel;
    int foodTickTimer;
    int playerGameType;
    boolean spawnExtraParticlesOnFall;
    short deathTime;
    short fire;
    short air;
    short sleepTimer;
    boolean fallFlying;
    boolean invulnerable;
    float foodExhaustionLevel;
    boolean seenCredits;

    int dataVersion;

    public static Player fromNbt(NbtCompound nbt) {
        Player player = new Player();

        if(nbt.has("abilities")) {
            player.abilities = Abilities.fromNbt(nbt.getCompound("abilities"));
        }
        if (nbt.has("recipeBook")) {
            player.recipeBook = RecipeBook.fromNbt(nbt.getCompound("recipeBook"));
        }
        if (nbt.has("HurtTime")) {
            player.hurtTime = nbt.getShort("HurtTime");
        }

        player.dataVersion = nbt.getInt("DataVersion");
        return player;
    }
}
