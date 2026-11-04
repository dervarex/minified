package com.dervarex.minified.worlds.entity;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.apiguardian.api.API;

/**
 * Extends {@link Entity} with common living entity fields
 */
@API(status = API.Status.STABLE)
public class LivingEntity extends Entity {

    /**
     * Wraps a living entity's NBT
     * @param raw the entity's NBT
     */
    public LivingEntity(NbtCompound raw) {
        super(raw);
    }

    /** {@return the health, in half hearts} */
    public float health() { return raw.getFloat("Health"); }

    /**
     * Sets the health
     * @param value the health in half hearts
     */
    public void setHealth(float value) { raw.setFloat("Health", value); }

    /** {@return whether the entity is left-handed} */
    public boolean leftHanded() { return raw.getBoolean("LeftHanded"); }

    /**
     * Sets whether the entity is left-handed
     * @param value true if it's left-handed
     */
    public void setLeftHanded(boolean value) { raw.setBoolean("LeftHanded", value); }

    /** {@return whether the entity picks up items} */
    public boolean canPickUpLoot() { return raw.getBoolean("CanPickUpLoot"); }

    /**
     * Sets whether the entity picks up items
     * @param value true if it picks up items
     */
    public void setCanPickUpLoot(boolean value) { raw.setBoolean("CanPickUpLoot", value); }

    /** {@return whether the entity never despawns} */
    public boolean persistenceRequired() { return raw.getBoolean("PersistenceRequired"); }

    /**
     * Sets whether the entity never despawns
     * @param value true if it never despawns
     */
    public void setPersistenceRequired(boolean value) { raw.setBoolean("PersistenceRequired", value); }

    /** {@return whether the entity's age stays the same} */
    public boolean ageLocked() { return raw.getBoolean("AgeLocked"); }

    /**
     * Sets whether the entity's age stays the same
     * @param value true if the age stays the same
     */
    public void setAgeLocked(boolean value) { raw.setBoolean("AgeLocked", value); }

    /** {@return the age in ticks, negative while it's a baby} */
    public int age() { return raw.getInt("Age"); }

    /**
     * Sets the age
     * @param value the age in ticks
     */
    public void setAge(int value) { raw.setInt("Age", value); }

    /** {@return the extra health from absorption, in half hearts} */
    public float absorptionAmount() { return raw.getFloat("AbsorptionAmount"); }

    /**
     * Sets the extra health from absorption
     * @param value the extra health in half hearts
     */
    public void setAbsorptionAmount(float value) { raw.setFloat("AbsorptionAmount", value); }

    /**
     * Raw access to "Brain" since it's deeply nested and varies on the entity, covering every entity would be too much effort here
     * @return the brain's NBT
     */
    public NbtCompound brain() { return raw.getCompound("Brain"); }
}