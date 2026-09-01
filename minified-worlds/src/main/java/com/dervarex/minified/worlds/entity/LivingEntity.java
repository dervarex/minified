package com.dervarex.minified.worlds.entity;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

/**
 * Extends {@link Entity} with common living entity fields
 */
public class LivingEntity extends Entity {

    public LivingEntity(NbtCompound raw) {
        super(raw);
    }

    public float health() { return raw.getFloat("Health"); }
    public void setHealth(float value) { raw.setFloat("Health", value); }

    public boolean leftHanded() { return raw.getBoolean("LeftHanded"); }
    public void setLeftHanded(boolean value) { raw.setBoolean("LeftHanded", value); }

    public boolean canPickUpLoot() { return raw.getBoolean("CanPickUpLoot"); }
    public void setCanPickUpLoot(boolean value) { raw.setBoolean("CanPickUpLoot", value); }

    public boolean persistenceRequired() { return raw.getBoolean("PersistenceRequired"); }
    public void setPersistenceRequired(boolean value) { raw.setBoolean("PersistenceRequired", value); }

    public boolean ageLocked() { return raw.getBoolean("AgeLocked"); }
    public void setAgeLocked(boolean value) { raw.setBoolean("AgeLocked", value); }

    public int age() { return raw.getInt("Age"); }
    public void setAge(int value) { raw.setInt("Age", value); }

    public float absorptionAmount() { return raw.getFloat("AbsorptionAmount"); }
    public void setAbsorptionAmount(float value) { raw.setFloat("AbsorptionAmount", value); }

    /** Raw access to "Brain" since it's deeply nested and varies on the entity, covering every entity would be too much effort here */
    public NbtCompound brain() { return raw.getCompound("Brain"); }
}