package com.dervarex.minified.worlds.entity;

import com.dervarex.minified.utils.nbt.tag.*;

import java.util.UUID;
import org.apiguardian.api.API;

/**
 * Wraps the fields confirmed common to every entity type in the Anvil entity format
 * See {@link LivingEntity} for the living entity tier, and use
 * raw() for anything type specific
 */
@API(status = API.Status.STABLE)
public class Entity {
    protected final NbtCompound raw;

    /**
     * Wraps an entity's NBT
     * @param raw the entity's NBT
     */
    public Entity(NbtCompound raw) {
        this.raw = raw;
    }

    /** {@return the entity's NBT} */
    public NbtCompound raw() { return raw; }

    /** {@return the entity type} */
    public String id() { return raw.getString("id"); }

    /** {@return the entity's UUID} */
    public UUID uuid() {
        int[] parts = raw.getIntArray("UUID");
        long most = ((long) parts[0] << 32) | (parts[1] & 0xFFFFFFFFL);
        long least = ((long) parts[2] << 32) | (parts[3] & 0xFFFFFFFFL);
        return new UUID(most, least);
    }

    /**
     * Sets the entity's UUID
     * @param uuid the new UUID
     */
    public void setUuid(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        raw.setIntArray("UUID", new int[]{
                (int) (most >> 32), (int) most,
                (int) (least >> 32), (int) least
        });
    }

    /** {@return the position, x, y, z} */
    public double[] pos() { return readDoubleTriplet("Pos"); }

    /**
     * Sets the position
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void setPos(double x, double y, double z) { writeDoubleTriplet("Pos", x, y, z); }

    /** {@return the motion in blocks per tick, x, y, z} */
    public double[] motion() { return readDoubleTriplet("Motion"); }

    /**
     * Sets the motion in blocks per tick
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void setMotion(double x, double y, double z) { writeDoubleTriplet("Motion", x, y, z); }

    /** {@return the rotation in degrees, yaw, pitch} */
    public float[] rotation() { return readFloatPair("Rotation"); }

    /**
     * Sets the rotation in degrees
     * @param yaw the yaw
     * @param pitch the pitch
     */
    public void setRotation(float yaw, float pitch) { writeFloatPair("Rotation", yaw, pitch); }

    /** {@return whether the entity is on the ground} */
    public boolean onGround() { return raw.getBoolean("OnGround"); }

    /**
     * Sets whether the entity is on the ground
     * @param value true if it's on the ground
     */
    public void setOnGround(boolean value) { raw.setBoolean("OnGround", value); }

    /** {@return whether the entity can't take damage} */
    public boolean invulnerable() { return raw.getBoolean("Invulnerable"); }

    /**
     * Sets whether the entity can't take damage
     * @param value true if it can't take damage
     */
    public void setInvulnerable(boolean value) { raw.setBoolean("Invulnerable", value); }

    /** {@return how far the entity has fallen, in blocks} */
    public double fallDistance() { return raw.getDouble("fall_distance"); }

    /**
     * Sets how far the entity has fallen
     * @param value the distance in blocks
     */
    public void setFallDistance(double value) { raw.setDouble("fall_distance", value); }

    /** {@return the air left, in ticks} */
    public short air() { return raw.getShort("Air"); }

    /**
     * Sets the air left
     * @param value the air in ticks
     */
    public void setAir(short value) { raw.setShort("Air", value); }

    /** {@return the ticks until the fire goes out, negative if it isn't burning} */
    public short fire() { return raw.getShort("Fire"); }

    /**
     * Sets the ticks until the fire goes out
     * @param value the fire ticks
     */
    public void setFire(short value) { raw.setShort("Fire", value); }

    /** {@return the ticks until the entity can use a portal again} */
    public int portalCooldown() { return raw.getInt("PortalCooldown"); }

    /**
     * Sets the ticks until the entity can use a portal again
     * @param value the cooldown in ticks
     */
    public void setPortalCooldown(int value) { raw.setInt("PortalCooldown", value); }

    /**
     * Reads a list of 3 doubles
     * @param key the NBT key
     * @return the 3 values
     */
    protected double[] readDoubleTriplet(String key) {
        NbtList list = raw.getList(key);
        return new double[]{
                ((NbtDouble) list.elements().get(0)).value(),
                ((NbtDouble) list.elements().get(1)).value(),
                ((NbtDouble) list.elements().get(2)).value()
        };
    }

    /**
     * Writes 3 doubles as a list
     * @param key the NBT key
     * @param x the first value
     * @param y the second value
     * @param z the third value
     */
    protected void writeDoubleTriplet(String key, double x, double y, double z) {
        NbtList list = new NbtList((byte) 6); // TAG_Double
        list.add(new NbtDouble(x));
        list.add(new NbtDouble(y));
        list.add(new NbtDouble(z));
        raw.setList(key, list);
    }

    /**
     * Reads a list of 2 floats
     * @param key the NBT key
     * @return the 2 values
     */
    protected float[] readFloatPair(String key) {
        NbtList list = raw.getList(key);
        return new float[]{
                ((NbtFloat) list.elements().get(0)).value(),
                ((NbtFloat) list.elements().get(1)).value()
        };
    }

    /**
     * Writes 2 floats as a list
     * @param key the NBT key
     * @param a the first value
     * @param b the second value
     */
    protected void writeFloatPair(String key, float a, float b) {
        NbtList list = new NbtList((byte) 5); // TAG_Float
        list.add(new NbtFloat(a));
        list.add(new NbtFloat(b));
        raw.setList(key, list);
    }
}