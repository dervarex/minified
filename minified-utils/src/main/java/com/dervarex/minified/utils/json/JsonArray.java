package com.dervarex.minified.utils.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class JsonArray implements JsonValue, Iterable<JsonValue> {
    private final List<JsonValue> values;

    public JsonArray() {
        this.values = new ArrayList<>();
    }

    public JsonArray(List<JsonValue> values) {
        this.values = new ArrayList<>(values == null ? List.of() : values);
    }

    public List<JsonValue> values() { return Collections.unmodifiableList(values); }

    public void add(JsonValue value) {
        this.values.add(value);
    }

    public int size() { return values.size(); }

    public JsonValue get(int index) { return values.get(index); }

    public JsonObject getObject(int index) {
        JsonValue value = values.get(index);
        return value == null ? null : value.asObject();
    }

    public JsonArray getArray(int index) {
        JsonValue value = values.get(index);
        return value == null ? null : value.asArray();
    }

    public String getString(int index) {
        JsonValue value = values.get(index);
        return value == null || value.isNull() ? null : value.asString();
    }

    public Boolean getBoolean(int index) {
        JsonValue value = values.get(index);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    public java.math.BigDecimal getNumber(int index) {
        JsonValue value = values.get(index);
        return value == null || value.isNull() ? null : value.asNumber();
    }

    @Override
    public JsonType getType() { return JsonType.ARRAY; }

    @Override
    public JsonArray asArray() { return this; }

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

    @Override
    public String toString() { return toJson(); }

    @Override
    public Iterator<JsonValue> iterator() {
        return values.iterator();
    }

    public void add(String value) {
        add(new JsonString(value));
    }

    public void add(Number value) {
        add(new JsonNumber(new BigDecimal(value.toString())));
    }

    public void add(boolean value) {
        add(new JsonBoolean(value));
    }

    public void addNull() {
        add(JsonNull.INSTANCE);
    }

    public JsonValue remove(int index) {
        return values.remove(index);
    }

    public void clear() {
        values.clear();
    }
}
