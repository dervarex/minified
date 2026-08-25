package com.dervarex.minified.utils.nbt.tag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class NbtCompound implements NbtTag {
    private final LinkedHashMap<String, NbtTag> entries = new LinkedHashMap<>();

    public byte id() { return 10; }

    public void put(String key, NbtTag value) {
        entries.put(key, value);
    }

    public void putString(String key, String value) { put(key, new NbtString(value)); }
    public void putInt(String key, int value)         { put(key, new NbtInt(value)); }
    public void putLong(String key, long value)        { put(key, new NbtLong(value)); }
    public void putByte(String key, byte value)        { put(key, new NbtByte(value)); }
    public void putBoolean(String key, boolean value)  { put(key, new NbtByte((byte) (value ? 1 : 0))); }
    public void putDouble(String key, double value)     { put(key, new NbtDouble(value)); }
    public void putFloat(String key, float value)        { put(key, new NbtFloat(value)); }
    public void putCompound(String key, NbtCompound value) { put(key, value); }
    public void putList(String key, NbtCompound value) { put(key, value); }

    public Optional<NbtTag> get(String key) {
        return Optional.ofNullable(entries.get(key));
    }

    public String getString(String key) {
        return get(key).filter(NbtString.class::isInstance)
                .map(NbtString.class::cast).map(NbtString::value)
                .orElseThrow(() -> new NoSuchElementException("No string tag: " + key));
    }

    public int getInt(String key) {
        return get(key).filter(NbtInt.class::isInstance)
                .map(NbtInt.class::cast).map(NbtInt::value)
                .orElseThrow(() -> new NoSuchElementException("No int tag: " + key));
    }

    public long getLong(String key) {
        return get(key).filter(NbtLong.class::isInstance)
                .map(NbtLong.class::cast).map(NbtLong::value)
                .orElseThrow(() -> new NoSuchElementException("No long tag: " + key));
    }

    public boolean getBoolean(String key) {
        return get(key).filter(NbtByte.class::isInstance)
                .map(NbtByte.class::cast).map(t -> t.value() != 0)
                .orElseThrow(() -> new NoSuchElementException("No byte tag: " + key));
    }

    public NbtCompound getCompound(String key) {
        return get(key).filter(NbtCompound.class::isInstance)
                .map(NbtCompound.class::cast)
                .orElseThrow(() -> new NoSuchElementException("No compound tag: " + key));
    }

    public NbtCompound getList(String key) {
        return get(key).filter(NbtCompound.class::isInstance)
                .map(NbtCompound.class::cast)
                .orElseThrow(() -> new NoSuchElementException("No list tag: " + key));
    }

    public boolean has(String key) { return entries.containsKey(key); }

    public Map<String, NbtTag> asMap() {
        return Map.copyOf(entries); // read only snapshot for the public
    }
}