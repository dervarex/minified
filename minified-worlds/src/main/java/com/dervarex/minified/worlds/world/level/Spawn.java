package com.dervarex.minified.worlds.world.level;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

public class Spawn {
    private String dimension;
    private int pitch;
    private int yaw;
    private int[] pos;

    public Spawn() {}

    public static Spawn fromNbt(NbtCompound spawnNbt) {
        Spawn spawn = new Spawn();
        spawn.dimension = spawnNbt.getString("dimension");
        spawn.pitch = spawnNbt.getInt("pitch");
        spawn.yaw = spawnNbt.getInt("yaw");
        spawn.pos = spawnNbt.getIntArray("pos").value();
        return spawn;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.setString("dimension", dimension);
        nbt.setInt("pitch", pitch);
        nbt.setInt("yaw", yaw);
        nbt.setIntArray("pos", pos);
        return nbt;
    }

    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }

    public int getPitch() { return pitch; }
    public void setPitch(int pitch) { this.pitch = pitch; }

    public int getYaw() { return yaw; }
    public void setYaw(int yaw) { this.yaw = yaw; }

    public int[] getPos() { return pos; }
    public void setPos(int[] pos) { this.pos = pos; }
}