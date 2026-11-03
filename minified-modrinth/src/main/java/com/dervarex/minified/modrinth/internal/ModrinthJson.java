package com.dervarex.minified.modrinth.internal;

import com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Small parsing helper around the project JSON utility types. Missing lists and maps come back empty.
 */
public final class ModrinthJson {
    private ModrinthJson() {
    }

    public static String string(JsonObject object, String key) {
        JsonValue value = value(object, key);
        return value == null ? null : value.asString();
    }

    /** {@code false} when missing */
    public static boolean bool(JsonObject object, String key) {
        JsonValue value = value(object, key);
        return value != null && value.asBoolean();
    }

    /** {@code 0} when missing */
    public static long number(JsonObject object, String key) {
        JsonValue value = value(object, key);
        return value == null ? 0L : value.asNumber().longValue();
    }

    /** {@code null} when missing */
    public static Integer integer(JsonObject object, String key) {
        JsonValue value = value(object, key);
        return value == null ? null : value.asNumber().intValue();
    }

    public static Instant instant(JsonObject object, String key) {
        String value = string(object, key);
        return value == null || value.isBlank() ? null : Instant.parse(value);
    }

    public static List<String> strings(JsonObject object, String key) {
        return list(object, key, JsonValue::asString);
    }

    public static <T> List<T> objects(JsonObject object, String key, Function<JsonObject, T> parser) {
        return list(object, key, value -> parser.apply(value.asObject()));
    }

    /** Applies {@code parser} to every non-null entry of the array */
    public static <T> List<T> list(JsonObject object, String key, Function<JsonValue, T> parser) {
        JsonArray array = array(object, key);
        if (array == null) {
            return List.of();
        }
        List<T> result = new ArrayList<>(array.size());
        for (JsonValue value : array) {
            if (value != null && !value.isNull()) {
                result.add(parser.apply(value));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static JsonObject object(JsonObject object, String key) {
        JsonValue value = value(object, key);
        return value == null ? null : value.asObject();
    }

    public static JsonArray array(JsonObject object, String key) {
        JsonValue value = value(object, key);
        return value == null ? null : value.asArray();
    }

    public static Map<String, String> stringMap(JsonObject object, String key) {
        JsonObject nested = object(object, key);
        if (nested == null) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String nestedKey : nested.keys()) {
            String value = string(nested, nestedKey);
            if (value != null) {
                result.put(nestedKey, value);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Runs a parser and turns whatever it throws into a {@link ModrinthSerializationException}, so a response with
     * an unexpected shape never escapes as a plain {@code ClassCastException} or similar
     */
    public static <T> T parse(JsonObject object, Function<JsonObject, T> parser) {
        try {
            return parser.apply(object);
        } catch (ModrinthSerializationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ModrinthSerializationException("Failed to parse Modrinth JSON payload", ex);
        }
    }

    private static JsonValue value(JsonObject object, String key) {
        if (object == null) {
            return null;
        }
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? null : value;
    }
}
