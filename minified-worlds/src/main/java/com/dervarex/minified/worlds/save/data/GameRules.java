package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.*;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class GameRules {
    @Setter
    private int dataVersion;
    private final Map<String, Boolean> booleanGamerules = new LinkedHashMap<>();
    private final Map<String, Integer> integerGamerules = new LinkedHashMap<>();

    public static GameRules fromNbt(NbtCompound nbt) {
        GameRules result = new GameRules();
        result.dataVersion = nbt.getInt("DataVersion");
        if (nbt.has("data")) {
            NbtCompound data = nbt.getCompound("data");
            for (Map.Entry<String, NbtTag> entry : data.asMap().entrySet()) {
                String key = entry.getKey();
                NbtTag tag = entry.getValue();
                if (tag instanceof NbtByte byteTag) {
                    result.booleanGamerules.put(key, byteTag.value() != 0);
                } else if (tag instanceof NbtInt intTag) {
                    result.integerGamerules.put(key, intTag.value());
                }
            }
        }
        return result;
    }
    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();
        for (Map.Entry<String, Boolean> entry : booleanGamerules.entrySet()) {
            data.setByte(entry.getKey(), (byte) (entry.getValue() ? 1 : 0));
        }
        for (Map.Entry<String, Integer> entry : integerGamerules.entrySet()) {
            data.setInt(entry.getKey(), entry.getValue());
        }
        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);
        return root;
    }

    public void putIntegerGamerule(String key, int value) {
        integerGamerules.put(key, value);
    }
    public void removeIntegerGamerule(String key) {
        integerGamerules.remove(key);
    }

    public void putBooleanGamerule(String key, boolean value) {
        booleanGamerules.put(key, value);
    }
    public void removeBooleanGamerule(String key) {
        booleanGamerules.remove(key);
    }
}