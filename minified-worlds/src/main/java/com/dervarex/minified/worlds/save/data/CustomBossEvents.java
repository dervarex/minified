package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class CustomBossEvents {
    @Setter
    private int dataVersion;
    private final Map<String, BossEvent> events = new LinkedHashMap<>();

    public static CustomBossEvents fromNbt(NbtCompound nbt) {
        CustomBossEvents result = new CustomBossEvents();
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
    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();
        for (Map.Entry<String, BossEvent> entry : events.entrySet()) {
            data.setCompound(entry.getKey(), entry.getValue().toNbt());
        }
        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }

    public void putEvent(String id, BossEvent event) {
        events.put(id, event);
    }
    public void removeEvent(String id) {
        events.remove(id);
    }
}