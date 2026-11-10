package com.dervarex.minified.utils.nbt.tag;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.apiguardian.api.API;

/**
 * Named tags, keeps the order of its keys
 */
@API(status = API.Status.STABLE)
public final class NbtCompound implements NbtTag {
    /**
     * Creates an empty compound
     */
    public NbtCompound() {
    }

    private final LinkedHashMap<String, NbtTag> entries = new LinkedHashMap<>();

    /**
     * {@return 10}
     */
    public byte id() { return 10; }

    /**
     * Sets a tag
     *
     * @param key the key
     * @param value the tag
     * @throws NullPointerException if the tag is null
     */
    public void put(String key, NbtTag value) {
        // crashing here beats crashing on save, where nobody knows which key it was
        entries.put(key, Objects.requireNonNull(value, () -> "NBT can't store null (key: " + key + ")"));
    }

    /**
     * Sets a string
     *
     * @param key the key
     * @param value the string
     */
    public void setString(String key, String value) { put(key, new NbtString(value)); }
    /**
     * Sets a int
     *
     * @param key the key
     * @param value the int
     */
    public void setInt(String key, int value)         { put(key, new NbtInt(value)); }
    /**
     * Sets a long
     *
     * @param key the key
     * @param value the long
     */
    public void setLong(String key, long value)        { put(key, new NbtLong(value)); }
    /**
     * Sets a short
     *
     * @param key the key
     * @param value the short
     */
    public void setShort(String key, short value)      { put(key, new NbtShort(value)); }
    /**
     * Sets a byte
     *
     * @param key the key
     * @param value the byte
     */
    public void setByte(String key, byte value)        { put(key, new NbtByte(value)); }
    /**
     * Sets a boolean
     *
     * @param key the key
     * @param value the boolean
     */
    public void setBoolean(String key, boolean value)  { put(key, new NbtBoolean(value)); }
    /**
     * Sets a double
     *
     * @param key the key
     * @param value the double
     */
    public void setDouble(String key, double value)     { put(key, new NbtDouble(value)); }
    /**
     * Sets a float
     *
     * @param key the key
     * @param value the float
     */
    public void setFloat(String key, float value)        { put(key, new NbtFloat(value)); }
    /**
     * Sets a byte array
     *
     * @param key the key
     * @param value the byte array
     */
    public void setByteArray(String key, byte[] value)  { put(key, new NbtByteArray(value)); }
    /**
     * Sets a int array
     *
     * @param key the key
     * @param value the int array
     */
    public void setIntArray(String key, int[] value)    { put(key, new NbtIntArray(value)); }
    /**
     * Sets a long array
     *
     * @param key the key
     * @param value the long array
     */
    public void setLongArray(String key, long[] value)  { put(key, new NbtLongArray(value)); }
    /**
     * Sets a compound
     *
     * @param key the key
     * @param value the compound
     */
    public void setCompound(String key, NbtCompound value) { put(key, value); }
    /**
     * Sets a list
     *
     * @param key the key
     * @param value the list
     */
    public void setList(String key, NbtList value) { put(key, value); }

    /**
     * {@return the tag, empty if it's missing}
     *
     * @param key the key
     */
    public Optional<NbtTag> get(String key) {
        return Optional.ofNullable(entries.get(key));
    }

    /**
     * {@return the boolean, a byte works too}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public boolean getBoolean(String key) {
        NbtTag tag = get(key).orElseThrow(() -> new NoSuchElementException("No byte tag: " + key));
        return switch (tag) {
            case NbtBoolean b -> b.value();
            case NbtByte b -> b.value() != 0;
            default -> throw new NoSuchElementException("No byte tag: " + key);
        };
    }

    /**
     * {@return the byte}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public byte getByte(String key) {
        return get(key).filter(NbtByte.class::isInstance)
                .map(NbtByte.class::cast).map(NbtByte::value)
                .orElseThrow(() -> new NoSuchElementException("No byte tag: " + key));
    }

    /**
     * {@return the byte array}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public byte[] getByteArray(String key) {
        return get(key).filter(NbtByteArray.class::isInstance)
                .map(NbtByteArray.class::cast).map(NbtByteArray::value)
                .orElseThrow(() -> new NoSuchElementException("No byte array tag: " + key));
    }

    /**
     * {@return the compound}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public NbtCompound getCompound(String key) {
        return get(key).filter(NbtCompound.class::isInstance)
                .map(NbtCompound.class::cast)
                .orElseThrow(() -> new NoSuchElementException("No compound tag: " + key));
    }

    /**
     * {@return the double}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public double getDouble(String key) {
        return get(key).filter(NbtDouble.class::isInstance)
                .map(NbtDouble.class::cast).map(NbtDouble::value)
                .orElseThrow(() -> new NoSuchElementException("No double tag: " + key));
    }

    /**
     * {@return the float}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public float getFloat(String key) {
        return get(key).filter(NbtFloat.class::isInstance)
                .map(NbtFloat.class::cast).map(NbtFloat::value)
                .orElseThrow(() -> new NoSuchElementException("No float tag: " + key));
    }

    /**
     * {@return the int}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public int getInt(String key) {
        return get(key).filter(NbtInt.class::isInstance)
                .map(NbtInt.class::cast).map(NbtInt::value)
                .orElseThrow(() -> new NoSuchElementException("No int tag: " + key));
    }

    /**
     * {@return the int array}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public int[] getIntArray(String key) {
        return get(key).filter(NbtIntArray.class::isInstance)
                .map(NbtIntArray.class::cast).map(NbtIntArray::value)
                .orElseThrow(() -> new NoSuchElementException("No int array tag: " + key));
    }

    /**
     * {@return the list}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public NbtList getList(String key) {
        return get(key).filter(NbtList.class::isInstance)
                .map(NbtList.class::cast)
                .orElseThrow(() -> new NoSuchElementException("No list tag: " + key));
    }

    /**
     * {@return the long}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public long getLong(String key) {
        return get(key).filter(NbtLong.class::isInstance)
                .map(NbtLong.class::cast).map(NbtLong::value)
                .orElseThrow(() -> new NoSuchElementException("No long tag: " + key));
    }

    /**
     * {@return the long array}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public long[] getLongArray(String key) {
        return get(key).filter(NbtLongArray.class::isInstance)
                .map(NbtLongArray.class::cast).map(NbtLongArray::value)
                .orElseThrow(() -> new NoSuchElementException("No long array tag: " + key));
    }

    /**
     * {@return the short}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public short getShort(String key) {
        return get(key).filter(NbtShort.class::isInstance)
                .map(NbtShort.class::cast).map(NbtShort::value)
                .orElseThrow(() -> new NoSuchElementException("No short tag: " + key));
    }

    /**
     * {@return the string}
     *
     * @param key the key
     * @throws NoSuchElementException if it's missing or another type
     */
    public String getString(String key) {
        return get(key).filter(NbtString.class::isInstance)
                .map(NbtString.class::cast).map(NbtString::value)
                .orElseThrow(() -> new NoSuchElementException("No string tag: " + key));
    }


    /**
     * {@return true if the key exists}
     *
     * @param key the key
     */
    public boolean has(String key) { return entries.containsKey(key); }

    /**
     * Removes a key
     * @param key the key
     * @return the tag that was stored under it, empty if there was none
     */
    public Optional<NbtTag> remove(String key) {
        return Optional.ofNullable(entries.remove(key));
    }

    /**
     * {@return a read only copy of the entries}
     */
    public Map<String, NbtTag> asMap() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(entries)); // read only snapshot for the public, keeps the order
    }

    /**
     * Deep copy, nested stuff included, same key order
     *
     * @return the copy
     */
    @Override
    public NbtCompound copy() {
        NbtCompound copy = new NbtCompound();
        entries.forEach((key, value) -> copy.entries.put(key, value.copy()));
        return copy;
    }
}