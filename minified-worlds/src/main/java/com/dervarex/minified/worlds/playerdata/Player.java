package com.dervarex.minified.worlds.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.nbt.tag.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

// a31ccf30-00e4-4928-a590-e366c90af710.dat (<player-uuid>.dat)

@Getter
@Setter
public class Player {

    @Nullable
    Advancements advancements;

    @Nullable
    Stats statistics;

    @Nullable
    Abilities abilities;

    @Nullable
    RecipeBook recipeBook;

    @Nullable
    Short hurtTime;

    @Nullable
    ArrayList<Double> motion; // e.g. 0, -0.0784000015258789, 0
    @Nullable
    ArrayList<Double> position; // e.g. 360.1760070593703, 77, 195.91003479668723
    @Nullable
    ArrayList<Double> rotation; // e.g. 155.59056, 49.508533

    @Nullable
    Attributes attributes;

    @Nullable
    ArrayList<Integer> UUID; // 4 integers -> UUID (most/least significant bits split in halves)

    @Nullable
    String dimension;
    @Nullable
    Double fallDistance;
    @Nullable
    Float absorptionAmount;
    @Nullable
    Float health;
    @Nullable
    Float XpP; // percent needed to reach the next xp level

    @Nullable
    Inventory inventory;

    @Nullable
    Float foodSaturationLevel;
    @Nullable
    Integer currentImpulseContextResetGraceTime;
    @Nullable
    Integer hurtByTimestamp;
    @Nullable
    Integer portalCooldown;
    @Nullable
    Integer score;
    @Nullable
    Integer selectedItemSlot;
    @Nullable
    Integer XPLevel;
    @Nullable
    Integer XPSeed;
    @Nullable
    Integer XPTotal;

    @Nullable
    WardenSpawnTracker wardenSpawnTracker;

    @Nullable
    Integer foodLevel;
    @Nullable
    Integer foodTickTimer;
    @Nullable
    Integer playerGameType;
    @Nullable
    Boolean spawnExtraParticlesOnFall;
    @Nullable
    Short deathTime;
    @Nullable
    Short fire;
    @Nullable
    Short air;
    @Nullable
    Short sleepTimer;
    @Nullable
    Boolean fallFlying;
    @Nullable
    Boolean invulnerable;
    @Nullable
    Float foodExhaustionLevel;
    @Nullable
    Boolean seenCredits;

    Integer dataVersion;

    public static Player fromNbt(@NotNull NbtCompound nbt, @Nullable JsonObject advancements, @Nullable JsonObject statistics) {
        Player player = new Player();

        if(advancements != null) {
            player.advancements = Advancements.fromJson(advancements);
        }
        if(statistics != null) {
            player.statistics = Stats.fromJson(statistics);
        }

        if(nbt.has("abilities")) {
            player.abilities = Abilities.fromNbt(nbt.getCompound("abilities"));
        }
        if (nbt.has("recipeBook")) {
            player.recipeBook = RecipeBook.fromNbt(nbt.getCompound("recipeBook"));
        }
        if (nbt.has("HurtTime")) {
            player.hurtTime = nbt.getShort("HurtTime");
        }
        if (nbt.has("Motion")) {
            player.motion = new ArrayList<>();
            for (NbtTag tag : nbt.getList("Motion").elements()) {
                player.motion.add(((NbtDouble) tag).value());
            }
        }
        if (nbt.has("Pos")) {
            player.position = new ArrayList<>();
            for (NbtTag tag : nbt.getList("Pos").elements()) {
                player.position.add(((NbtDouble) tag).value());
            }
        }
        if (nbt.has("Rotation")) {
            player.rotation = new ArrayList<>();
            for (NbtTag tag : nbt.getList("Rotation").elements()) {
                player.rotation.add((double) ((NbtFloat) tag).value());
            }
        }
        if(nbt.has("attributes")) {
            player.attributes = Attributes.fromNbtList(nbt.getList("attributes"));
        }
        if (nbt.has("UUID")) {
            player.UUID = new ArrayList<>();
            for (int part : nbt.getIntArray("UUID").value()) {
                player.UUID.add(part);
            }
        }
        if(nbt.has("Dimension")) {
            player.dimension = nbt.getString("Dimension");
        }
        if (nbt.has("fall_distance")) {
            player.fallDistance = nbt.getDouble("fall_distance");
        }
        if(nbt.has("AbsorptionAmount")) {
            player.absorptionAmount = nbt.getFloat("AbsorptionAmount");
        }
        if (nbt.has("Health")) {
            player.health = nbt.getFloat("Health");
        }
        if (nbt.has("XpP")) {
            player.XpP = nbt.getFloat("XpP");
        }
        if (nbt.has("Inventory")) {
            player.inventory = Inventory.fromNbtList(nbt.getList("Inventory"));
        }
        if (nbt.has("foodSaturationLevel")) {
            player.foodSaturationLevel = nbt.getFloat("foodSaturationLevel");
        }
        if (nbt.has("current_impulse_context_reset_grace_time")) {
            player.currentImpulseContextResetGraceTime = nbt.getInt("current_impulse_context_reset_grace_time");
        }
        if (nbt.has("HurtByTimestamp")) {
            player.hurtByTimestamp = nbt.getInt("HurtByTimestamp");
        }
        if (nbt.has("PortalCooldown")) {
            player.portalCooldown = nbt.getInt("PortalCooldown");
        }
        if (nbt.has("Score")) {
            player.score = nbt.getInt("Score");
        }
        if (nbt.has("SelectedItemSlot")) {
            player.selectedItemSlot = nbt.getInt("SelectedItemSlot");
        }
        if (nbt.has("XpLevel")) {
            player.XPLevel = nbt.getInt("XpLevel");
        }
        if (nbt.has("XpSeed")) {
            player.XPSeed = nbt.getInt("XpSeed");
        }
        if (nbt.has("XpTotal")) {
            player.XPTotal = nbt.getInt("XpTotal");
        }
        if (nbt.has("warden_spawn_tracker")) {
            player.wardenSpawnTracker = WardenSpawnTracker.fromNbt(nbt.getCompound("warden_spawn_tracker"));
        }
        if (nbt.has("foodLevel")) {
            player.foodLevel = nbt.getInt("foodLevel");
        }
        if (nbt.has("foodTickTimer")) {
            player.foodTickTimer = nbt.getInt("foodTickTimer");
        }
        if (nbt.has("playerGameType")) {
            player.playerGameType = nbt.getInt("playerGameType");
        }
        if (nbt.has("spawn_extra_particles_on_fall")) {
            player.spawnExtraParticlesOnFall = nbt.getBoolean("spawn_extra_particles_on_fall");
        }
        if (nbt.has("DeathTime")) {
            player.deathTime = nbt.getShort("DeathTime");
        }
        if (nbt.has("Fire")) {
            player.fire = nbt.getShort("Fire");
        }
        if (nbt.has("Air")) {
            player.air = nbt.getShort("Air");
        }
        if (nbt.has("SleepTimer")) {
            player.sleepTimer = nbt.getShort("SleepTimer");
        }
        if (nbt.has("FallFlying")) {
            player.fallFlying = nbt.getBoolean("FallFlying");
        }
        if (nbt.has("Invulnerable")) {
            player.invulnerable = nbt.getBoolean("Invulnerable");
        }
        if (nbt.has("foodExhaustionLevel")) {
            player.foodExhaustionLevel = nbt.getFloat("foodExhaustionLevel");
        }
        if (nbt.has("seenCredits")) {
            player.seenCredits = nbt.getBoolean("seenCredits");
        }

        player.dataVersion = nbt.getInt("DataVersion");
        return player;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        if (abilities != null) {
            nbt.setCompound("abilities", abilities.toNbt());
        }
        if (recipeBook != null) {
            nbt.setCompound("recipeBook", recipeBook.toNbt());
        }
        if (hurtTime != null) {
            nbt.setShort("HurtTime", hurtTime);
        }
        if (motion != null) {
            NbtList list = new NbtList((byte) 6); // NbtDouble
            for (double d : motion) {
                list.add(new NbtDouble(d));
            }
            nbt.setList("Motion", list);
        }
        if (position != null) {
            NbtList list = new NbtList((byte) 6); // NbtDouble
            for (double d : position) {
                list.add(new NbtDouble(d));
            }
            nbt.setList("Pos", list);
        }
        if (rotation != null) {
            NbtList list = new NbtList((byte) 5); // NbtFloat
            for (double d : rotation) {
                list.add(new NbtFloat((float) d));
            }
            nbt.setList("Rotation", list);
        }
        if (attributes != null) {
            nbt.setList("attributes", attributes.toNbtList());
        }
        if (UUID != null) {
            int[] arr = new int[UUID.size()];
            for (int i = 0; i < UUID.size(); i++) {
                arr[i] = UUID.get(i);
            }
            nbt.setIntArray("UUID", arr);
        }
        if (dimension != null) {
            nbt.setString("Dimension", dimension);
        }
        if (fallDistance != null) {
            nbt.setDouble("fall_distance", fallDistance);
        }
        if (absorptionAmount != null) {
            nbt.setFloat("AbsorptionAmount", absorptionAmount);
        }
        if (health != null) {
            nbt.setFloat("Health", health);
        }
        if (XpP != null) {
            nbt.setFloat("XpP", XpP);
        }
        if (inventory != null) {
            nbt.setList("Inventory", inventory.toNbtList());
        }
        if (foodSaturationLevel != null) {
            nbt.setFloat("foodSaturationLevel", foodSaturationLevel);
        }
        if (currentImpulseContextResetGraceTime != null) {
            nbt.setInt("current_impulse_context_reset_grace_time", currentImpulseContextResetGraceTime);
        }
        if (hurtByTimestamp != null) {
            nbt.setInt("HurtByTimestamp", hurtByTimestamp);
        }
        if (portalCooldown != null) {
            nbt.setInt("PortalCooldown", portalCooldown);
        }
        if (score != null) {
            nbt.setInt("Score", score);
        }
        if (selectedItemSlot != null) {
            nbt.setInt("SelectedItemSlot", selectedItemSlot);
        }
        if (XPLevel != null) {
            nbt.setInt("XpLevel", XPLevel);
        }
        if (XPSeed != null) {
            nbt.setInt("XpSeed", XPSeed);
        }
        if (XPTotal != null) {
            nbt.setInt("XpTotal", XPTotal);
        }
        if (wardenSpawnTracker != null) {
            nbt.setCompound("warden_spawn_tracker", wardenSpawnTracker.toNbt());
        }
        if (foodLevel != null) {
            nbt.setInt("foodLevel", foodLevel);
        }
        if (foodTickTimer != null) {
            nbt.setInt("foodTickTimer", foodTickTimer);
        }
        if (playerGameType != null) {
            nbt.setInt("playerGameType", playerGameType);
        }
        if (spawnExtraParticlesOnFall != null) {
            nbt.setBoolean("spawn_extra_particles_on_fall", spawnExtraParticlesOnFall);
        }
        if (deathTime != null) {
            nbt.setShort("DeathTime", deathTime);
        }
        if (fire != null) {
            nbt.setShort("Fire", fire);
        }
        if (air != null) {
            nbt.setShort("Air", air);
        }
        if (sleepTimer != null) {
            nbt.setShort("SleepTimer", sleepTimer);
        }
        if (fallFlying != null) {
            nbt.setBoolean("FallFlying", fallFlying);
        }
        if (invulnerable != null) {
            nbt.setBoolean("Invulnerable", invulnerable);
        }
        if (foodExhaustionLevel != null) {
            nbt.setFloat("foodExhaustionLevel", foodExhaustionLevel);
        }
        if (seenCredits != null) {
            nbt.setBoolean("seenCredits", seenCredits);
        }
        if (dataVersion != null) {
            nbt.setInt("DataVersion", dataVersion);
        }

        return nbt;
    }
}
