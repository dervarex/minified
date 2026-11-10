package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apiguardian.api.API;

/**
 * The boss bars made with /bossbar, from custom_boss_events.dat
 */
@Getter
@API(status = API.Status.STABLE)
public class CustomBossEvents {
    /**
     * Creates empty custom boss bars
     */
    public CustomBossEvents() {
    }

    @Setter
    private int dataVersion;
    private final Map<String, BossEvent> events = new LinkedHashMap<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the boss bars from NBT
     * @param nbt the content of custom_boss_events.dat
     * @return the boss bars
     */
    public static CustomBossEvents fromNbt(NbtCompound nbt) {
        CustomBossEvents result = new CustomBossEvents();
        result.raw = nbt.copy();
        result.dataVersion = nbt.getInt("DataVersion");
        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");
            for (Map.Entry<String, NbtTag> entry : data.asMap().entrySet()) {
                if (entry.getValue() instanceof NbtCompound eventCompound) {
                    result.events.put(entry.getKey(), BossEvent.fromNbt(eventCompound));
                }
            }
        }
        return result;
    }

    /** {@return the boss bars as NBT, like in custom_boss_events.dat} */
    public NbtCompound toNbt() {
        NbtCompound root = RawNbt.copyOrNew(raw);
        NbtCompound data = RawNbt.child(root, "data");
        RawNbt.removeMissing(data, events.keySet(), tag -> tag instanceof NbtCompound);
        for (Map.Entry<String, BossEvent> entry : events.entrySet()) {
            data.setCompound(entry.getKey(), entry.getValue().toNbt());
        }
        root.setInt("DataVersion", dataVersion);
        return root;
    }

    /**
     * Adds or replaces a boss bar
     * @param id the boss bar's id
     * @param event the boss bar
     */
    public void putEvent(String id, BossEvent event) {
        events.put(id, event);
    }

    /**
     * Removes a boss bar
     * @param id the boss bar's id
     */
    public void removeEvent(String id) {
        events.remove(id);
    }
}