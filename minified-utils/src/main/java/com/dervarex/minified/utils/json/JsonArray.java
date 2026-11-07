package com.dervarex.minified.utils.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A JSON array
 *
 * @param values the values
 */
public record JsonArray(List<JsonValue> values) implements JsonValue, Iterable<JsonValue> {
    /**
     * An empty array
     */
    public JsonArray() {
        this(new ArrayList<>());
    }

    /**
     * @param values the values, copied, null means empty
     */
    public JsonArray(List<JsonValue> values) {
        this.values = new ArrayList<>(values == null ? List.of() : values);
    }

    @Override
    public List<JsonValue> values() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Adds a value
     *
     * @param value the value
     */
    public void add(JsonValue value) {
        this.values.add(value);
    }

    /**
     * {@return how many values there are}
     */
    public int size() {
        return values.size();
    }

    /**
     * {@return the value at the index}
     *
     * @param index the index
     */
    public JsonValue get(int index) {
        return values.get(index);
    }

    /**
     * {@return the object at the index, null if it's null}
     *
     * @param index the index
     */
    public JsonObject getObject(int index) {
        JsonValue value = values.get(index);
        return value == null || value.isNull() ? null : value.asObject();
    }

    /**
     * {@return the array at the index, null if it's null}
     *
     * @param index the index
     */
    public JsonArray getArray(int index) {
        JsonValue value = values.get(index);
        return value == null || value.isNull() ? null : value.asArray();
    }

    /**
     * {@return the string at the index, null if it's null}
     *
     * @param index the index
     */
    public String getString(int index) {
        JsonValue value = values.get(index);
        return value == null || value.isNull() ? null : value.asString();
    }

    /**
     * {@return the boolean at the index, null if it's null}
     *
     * @param index the index
     */
    public Boolean getBoolean(int index) {
        JsonValue value = values.get(index);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    /**
     * {@return the number at the index, null if it's null}
     *
     * @param index the index
     */
    public BigDecimal getNumber(int index) {
        JsonValue value = values.get(index);
        return value == null || value.isNull() ? null : value.asNumber();
    }

    /**
     * {@return {@link JsonType#ARRAY}}
     */
    @Override
    public JsonType getType() {
        return JsonType.ARRAY;
    }

    /**
     * {@return this}
     */
    @Override
    public JsonArray asArray() {
        return this;
    }

    /**
     * {@return this as JSON text}
     */
    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            JsonValue value = values.get(i);
            sb.append(value == null ? "null" : value.toJson());
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * {@return same as {@link #toJson()}}
     */
    @Override
    public String toString() {
        return toJson();
    }

    /**
     * {@return an iterator over the values}
     */
    @Override
    public Iterator<JsonValue> iterator() {
        return values.iterator();
    }

    /**
     * Like {@link #add(JsonValue)}, but for a string
     *
     * @param value the string
     */
    public void add(String value) {
        add(new JsonString(value));
    }

    /**
     * Like {@link #add(JsonValue)}, but for a number
     *
     * @param value the number
     */
    public void add(Number value) {
        add(new JsonNumber(new BigDecimal(value.toString())));
    }

    /**
     * Like {@link #add(JsonValue)}, but for a boolean
     *
     * @param value the boolean
     */
    public void add(boolean value) {
        add(new JsonBoolean(value));
    }

    /**
     * Adds a null
     */
    public void addNull() {
        add(JsonNull.INSTANCE);
    }

    /**
     * Removes the value at the index
     *
     * @param index the index
     * @return the removed value
     */
    public JsonValue remove(int index) {
        return values.remove(index);
    }

    /**
     * Removes all values
     */
    public void clear() {
        values.clear();
    }
}
