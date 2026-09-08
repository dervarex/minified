package com.dervarex.minified.worlds.entity;

import com.dervarex.minified.utils.nbt.tag.*;

import java.util.UUID;

/**
 * Wraps the fields confirmed common to every entity type in the Anvil entity format
 * See {@link LivingEntity} for the living entity tier, and use
 * raw() for anything type specific
 */
public class Entity {
    protected final NbtCompound raw;

    public Entity(NbtCompound raw) {
        this.raw = raw;
    }

    public NbtCompound raw() { return raw; }

    public String id() { return raw.getString("id"); }

    public UUID uuid() {
        int[] parts = raw.getIntArray("UUID").value();
        long most = ((long) parts[0] << 32) | (parts[1] & 0xFFFFFFFFL);
        long least = ((long) parts[2] << 32) | (parts[3] & 0xFFFFFFFFL);
        return new UUID(most, least);
    }

    public void setUuid(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        raw.setIntArray("UUID", new int[]{
                (int) (most >> 32), (int) most,
                (int) (least >> 32), (int) least
        });
    }

    public double[] pos() { return readDoubleTriplet("Pos"); }
    public void setPos(double x, double y, double z) { writeDoubleTriplet("Pos", x, y, z); }

    public double[] motion() { return readDoubleTriplet("Motion"); }
    public void setMotion(double x, double y, double z) { writeDoubleTriplet("Motion", x, y, z); }

    /** [yaw, pitch] */
    public float[] rotation() { return readFloatPair("Rotation"); }
    public void setRotation(float yaw, float pitch) { writeFloatPair("Rotation", yaw, pitch); }

    public boolean onGround() { return raw.getBoolean("OnGround"); }
    public void setOnGround(boolean value) { raw.setBoolean("OnGround", value); }

    public boolean invulnerable() { return raw.getBoolean("Invulnerable"); }
    public void setInvulnerable(boolean value) { raw.setBoolean("Invulnerable", value); }

    public double fallDistance() { return raw.getDouble("fall_distance"); }
    public void setFallDistance(double value) { raw.setDouble("fall_distance", value); }

    public short air() { return raw.getShort("Air"); }
    public void setAir(short value) { raw.setShort("Air", value); }

    public short fire() { return raw.getShort("Fire"); }
    public void setFire(short value) { raw.setShort("Fire", value); }

    public int portalCooldown() { return raw.getInt("PortalCooldown"); }
    public void setPortalCooldown(int value) { raw.setInt("PortalCooldown", value); }

    protected double[] readDoubleTriplet(String key) {
        NbtList list = raw.getList(key);
        return new double[]{
                ((NbtDouble) list.elements().get(0)).value(),
                ((NbtDouble) list.elements().get(1)).value(),
                ((NbtDouble) list.elements().get(2)).value()
        };
    }

    protected void writeDoubleTriplet(String key, double x, double y, double z) {
        NbtList list = new NbtList((byte) 6); // TAG_Double
        list.add(new NbtDouble(x));
        list.add(new NbtDouble(y));
        list.add(new NbtDouble(z));
        raw.setList(key, list);
    }

    protected float[] readFloatPair(String key) {
        NbtList list = raw.getList(key);
        return new float[]{
                ((NbtFloat) list.elements().get(0)).value(),
                ((NbtFloat) list.elements().get(1)).value()
        };
    }

    protected void writeFloatPair(String key, float a, float b) {
        NbtList list = new NbtList((byte) 5); // TAG_Float
        list.add(new NbtFloat(a));
        list.add(new NbtFloat(b));
        raw.setList(key, list);
    }
}