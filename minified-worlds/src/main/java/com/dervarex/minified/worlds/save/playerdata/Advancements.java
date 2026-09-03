package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class Advancements {
    @Setter
    private int dataVersion;
    private final Map<String, Advancement> advancements = new LinkedHashMap<>();

    public static Advancements fromJson(JsonObject json) {
        Advancements result = new Advancements();

        if (json.has("DataVersion")) {
            result.dataVersion = json.get("DataVersion").asInt();
        }

        for (String key : json.keys()) {
            if ("DataVersion".equals(key)) {
                continue;
            }

            JsonValue value = json.get(key);
            if (value != null && value.isObject()) {
                result.advancements.put(key, Advancement.fromJson(value.asObject()));
            }
        }
        return result;
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();

        for (Map.Entry<String, Advancement> entry : advancements.entrySet()) {
            root.put(entry.getKey(), entry.getValue().toJson());
        }
        root.put("DataVersion", dataVersion);

        return root;
    }

    @Getter
    @Setter
    public static class Advancement {
        private boolean done;
        private final Map<String, String> criteria = new LinkedHashMap<>();

        public static Advancement fromJson(JsonObject json) {
            Advancement adv = new Advancement();

            if (json.has("done")) {
                adv.done = json.get("done").asBoolean();
            }

            if (json.has("criteria")) {
                JsonValue criteriaValue = json.get("criteria");
                if (criteriaValue != null && criteriaValue.isObject()) {
                    JsonObject criteriaObj = criteriaValue.asObject();
                    for (String key : criteriaObj.keys()) {
                        adv.criteria.put(key, criteriaObj.get(key).asString());
                    }
                }
            }
            return adv;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.put("done", done);

            JsonObject criteriaObj = new JsonObject();
            for (Map.Entry<String, String> entry : criteria.entrySet()) {
                criteriaObj.put(entry.getKey(), entry.getValue());
            }
            json.put("criteria", criteriaObj);

            return json;
        }

        @Nullable
        public String getCriterionDate(String criterionKey) {
            return criteria.get(criterionKey);
        }
    }
}