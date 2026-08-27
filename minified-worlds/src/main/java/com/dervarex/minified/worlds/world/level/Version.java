package com.dervarex.minified.worlds.world.level;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

public class Version {
    private int id;
    private String name;
    private String series;
    private boolean snapshot;

    public Version() {}

    public static Version fromNbt(NbtCompound versionNbt) {
        Version version = new Version();
        version.id = versionNbt.getInt("Id");
        version.name = versionNbt.getString("Name");
        version.series = versionNbt.getString("Series");
        version.snapshot = versionNbt.getBoolean("Snapshot");
        return version;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("Id", id);
        nbt.setString("Name", name);
        nbt.setString("Series", series);
        nbt.setBoolean("Snapshot", snapshot);
        return nbt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public boolean isSnapshot() { return snapshot; }
    public void setSnapshot(boolean snapshot) { this.snapshot = snapshot; }
}