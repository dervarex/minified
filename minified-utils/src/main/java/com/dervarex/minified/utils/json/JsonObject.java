package com.dervarex.minified.utils.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class JsonObject implements JsonValue {
    private final LinkedHashMap<String, JsonValue> values;

    public JsonObject(Map<String, JsonValue> values) {
        this.values = new LinkedHashMap<>(values == null ? Map.of() : values);
    }

    public Set<String> keys() { return Collections.unmodifiableSet(values.keySet()); }
    public boolean containsKey(String key) { return values.containsKey(key); }

    public Map<String, JsonValue> entries() { return Collections.unmodifiableMap(values); }

    public JsonValue get(String key) { return values.get(key); }

    public boolean has(String key) { return values.containsKey(key); }

    public JsonObject getObject(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asObject();
    }

    public JsonArray getArray(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asArray();
    }

    public String getString(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asString();
    }

    public java.math.BigDecimal getNumber(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asNumber();
    }

    public Boolean getBoolean(String key) {
        JsonValue value = values.get(key);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    public Integer getInt(String key) {
        java.math.BigDecimal number = getNumber(key);
        return number == null ? null : number.intValue();
    }

    public Long getLong(String key) {
        java.math.BigDecimal number = getNumber(key);
        return number == null ? null : number.longValue();
    }

    public Double getDouble(String key) {
        java.math.BigDecimal number = getNumber(key);
        return number == null ? null : number.doubleValue();
    }

    @Override
    public JsonType getType() { return JsonType.OBJECT; }

    @Override
    public JsonObject asObject() { return this; }

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

    @Override
    public String toString() { return toJson(); }
}

