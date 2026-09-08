package com.dervarex.minified.worlds.save.level;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
public class Version {
    private int id;
    @Nullable
    private String name;
    @Nullable
    private String series;
    private boolean snapshot;

    public static Version fromNbt(NbtCompound versionNbt) {
        Version version = new Version();
        if (versionNbt.has("Id")) {
            version.id = versionNbt.getInt("Id");
        }
        if (versionNbt.has("Name")) {
            version.name = versionNbt.getString("Name");
        }
        if (versionNbt.has("Series")) {
            version.series = versionNbt.getString("Series");
        }
        if (versionNbt.has("Snapshot")) {
            version.snapshot = versionNbt.getByte("Snapshot") != 0;
        }
        return version;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.setInt("Id", id);
        if (name != null) {
            nbt.setString("Name", name);
        }
        if (series != null) {
            nbt.setString("Series", series);
        }
        nbt.setByte("Snapshot", (byte) (snapshot ? 1 : 0));
        return nbt;
    }
}