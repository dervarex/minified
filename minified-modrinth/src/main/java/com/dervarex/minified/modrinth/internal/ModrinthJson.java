package com.dervarex.minified.modrinth.internal;

import com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Small parsing helper around the project JSON utility types.
 */
public final class ModrinthJson {
    private ModrinthJson() {
    }

    public static String string(JsonObject object, String key) {
        if (object == null) {
            return null;
        }
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? null : value.asString();
    }

    public static Boolean bool(JsonObject object, String key) {
        if (object == null) {
            return null;
        }
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    public static Long longValue(JsonObject object, String key) {
        if (object == null) {
            return null;
        }
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? null : value.asNumber().longValue();
    }

    public static Integer integer(JsonObject object, String key) {
        Long value = longValue(object, key);
        return value == null ? null : value.intValue();
    }

    public static Instant instant(JsonObject object, String key) {
        String value = string(object, key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return Instant.parse(value);
    }

    public static String[] strings(JsonObject object, String key) {
        JsonArray array = array(object, key);
        if (array == null) {
            return null;
        }
        String[] result = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.getString(i);
        }
        return result;
    }

    public static JsonObject object(JsonObject object, String key) {
        if (object == null) {
            return null;
        }
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? null : value.asObject();
    }

    public static JsonArray array(JsonObject object, String key) {
        if (object == null) {
            return null;
        }
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? null : value.asArray();
    }

    public static Map<String, String> stringMap(JsonObject object, String key) {
        JsonObject nested = object(object, key);
        if (nested == null) {
            return null;
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String nestedKey : nested.keys()) {
            JsonValue value = nested.get(nestedKey);
            result.put(nestedKey, value == null || value.isNull() ? null : value.asString());
        }
        return result;
    }

    public static <T> T parse(JsonObject object, Function<JsonObject, T> parser) {
        try {
            return parser.apply(object);
        } catch (RuntimeException ex) {
            throw new ModrinthSerializationException("Failed to parse Modrinth JSON payload", ex);
        }
    }

    public static <E extends Enum<E>> E enumValue(JsonObject object, String key, Function<String, E> parser) {
        String value = string(object, key);
        return value == null ? null : parser.apply(value);
    }

    public static String[] copyOf(String[] values) {
        return values == null ? null : Arrays.stream(values)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }
}

