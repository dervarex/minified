package com.dervarex.minified.utils.json;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A JSON object, keeps the order of its keys
 */
public final class JsonObject implements JsonValue {
    private final LinkedHashMap<String, JsonValue> values;

    /**
     * @param values the entries, copied, null means empty
     */
    public JsonObject(Map<String, JsonValue> values) {
        this.values = new LinkedHashMap<>(values == null ? Map.of() : values);
    }
    /**
     * An empty object
     */
    public JsonObject() {
        this.values = new LinkedHashMap<>();
    }
    /**
     * Sets a value
     *
     * @param key the key
     * @param value the value, null becomes a JSON null
     */
    public void put(String key, JsonValue value) {
        values.put(key, value == null ? JsonNull.INSTANCE : value);
    }

    /**
     * Like {@link #put(String, JsonValue)}, but for a string
     *
     * @param key the key
     * @param value the string
     */
    public void put(String key, String value) {
        put(key, new JsonString(value));
    }

    /**
     * Like {@link #put(String, JsonValue)}, but for a number
     *
     * @param key the key
     * @param value the number
     */
    public void put(String key, Number value) {
        put(key, new JsonNumber(new BigDecimal(value.toString())));
    }

    /**
     * Like {@link #put(String, JsonValue)}, but for a boolean
     *
     * @param key the key
     * @param value the boolean
     */
    public void put(String key, boolean value) {
        put(key, new JsonBoolean(value));
    }

    /**
     * Sets a key to null
     *
     * @param key the key
     */
    public void putNull(String key) {
        put(key, JsonNull.INSTANCE);
    }

    /**
     * Removes a key
     *
     * @param key the key
     * @return the removed value, null if there was none
     */
    public JsonValue remove(String key) {
        return values.remove(key);
    }

    /**
     * Removes all keys
     */
    public void clear() {
        values.clear();
    }

    /**
     * {@return how many keys there are}
     */
    public int size() {
        return values.size();
    }

    /**
     * {@return the keys}
     */
    public Set<String> keys() { return Collections.unmodifiableSet(values.keySet()); }
    /**
     * {@return true if the key exists}
     *
     * @param key the key
     */
    public boolean containsKey(String key) { return values.containsKey(key); }

    /**
     * {@return the keys with their values}
     */
    public Map<String, JsonValue> entries() { return Collections.unmodifiableMap(values); }

    /**
     * {@return the value, null if it's missing}
     *
     * @param key the key
     */
    public JsonValue get(String key) { return values.get(key); }

    /**
     * Same as {@link #containsKey(String)}
     *
     * @param key the key
     * @return true if the key exists
     */
    public boolean has(String key) { return values.containsKey(key); }

    /**
     * {@return the object, null if it's missing or null}
     *
     * @param key the key
     */
    public JsonObject getObject(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asObject();
    }

    /**
     * {@return the array, null if it's missing or null}
     *
     * @param key the key
     */
    public JsonArray getArray(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asArray();
    }

    /**
     * {@return the string, null if it's missing or null}
     *
     * @param key the key
     */
    public String getString(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asString();
    }

    /**
     * {@return the number, null if it's missing or null}
     *
     * @param key the key
     */
    public java.math.BigDecimal getNumber(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asNumber();
    }

    /**
     * {@return the boolean, null if it's missing or null}
     *
     * @param key the key
     */
    public Boolean getBoolean(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    /**
     * {@return the number as an int, null if it's missing or null}
     *
     * @param key the key
     */
    public Integer getInt(String key) {
        java.math.BigDecimal number = getNumber(key);
        return number == null ? null : number.intValue();
    }

    /**
     * {@return the number as a long, null if it's missing or null}
     *
     * @param key the key
     */
    public Long getLong(String key) {
        java.math.BigDecimal number = getNumber(key);
        return number == null ? null : number.longValue();
    }

    /**
     * {@return the number as a double, null if it's missing or null}
     *
     * @param key the key
     */
    public Double getDouble(String key) {
        java.math.BigDecimal number = getNumber(key);
        return number == null ? null : number.doubleValue();
    }

    /**
     * {@return {@link JsonType#OBJECT}}
     */
    @Override
    public JsonType getType() { return JsonType.OBJECT; }

    /**
     * {@return this}
     */
    @Override
    public JsonObject asObject() { return this; }

    /**
     * {@return this as JSON text}
     */
    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, JsonValue> entry : values.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(JsonWriter.escape(entry.getKey())).append('"').append(':');
            JsonValue value = entry.getValue();
            sb.append(value == null ? "null" : value.toJson());
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * {@return true if it's an object with the same entries}
     *
     * @param o the other object
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof JsonObject other && values.equals(other.values);
    }

    /**
     * {@return the hash code of the entries}
     */
    @Override
    public int hashCode() { return values.hashCode(); }

    /**
     * {@return same as {@link #toJson()}}
     */
    @Override
    public String toString() { return toJson(); }
}

