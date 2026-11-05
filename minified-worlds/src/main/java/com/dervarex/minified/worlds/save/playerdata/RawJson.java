package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import com.dervarex.minified.utils.json.JsonValue;

import java.util.Set;

final class RawJson {
    private RawJson() {}

    static JsonObject copy(JsonObject json) {
        return JsonParser.parse(json.toJson()).asObject();
    }

    static JsonObject copyOrNew(JsonObject raw) {
        return raw != null ? copy(raw) : new JsonObject();
    }

    static void removeMissingObjects(JsonObject json, Set<String> keep) {
        for (String key : Set.copyOf(json.keys())) {
            JsonValue value = json.get(key);
            if (!keep.contains(key) && value != null && value.isObject()) {
                json.remove(key);
            }
        }
    }
}
