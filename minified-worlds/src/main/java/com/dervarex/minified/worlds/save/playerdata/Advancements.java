package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apiguardian.api.API;

/**
 * The advancements of a player, from advancements/&lt;uuid&gt;.json
 */
@Getter
@API(status = API.Status.STABLE)
public class Advancements {
    @Setter
    private int dataVersion;
    private final Map<String, Advancement> advancements = new LinkedHashMap<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private JsonObject raw;

    /**
     * Reads the advancements from JSON
     * @param json the content of the player's advancements file
     * @return the advancements
     */
    public static Advancements fromJson(JsonObject json) {
        Advancements result = new Advancements();
        result.raw = RawJson.copy(json);

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

    /** {@return the advancements as JSON, like in the advancements file} */
    public JsonObject toJson() {
        JsonObject root = RawJson.copyOrNew(raw);
        RawJson.removeMissingObjects(root, advancements.keySet());

        for (Map.Entry<String, Advancement> entry : advancements.entrySet()) {
            root.put(entry.getKey(), entry.getValue().toJson());
        }
        root.put("DataVersion", dataVersion);

        return root;
    }

    /**
     * One advancement and when each of its criteria was done
     */
    @Getter
    @Setter
    public static class Advancement {
        private boolean done;
        private final Map<String, String> criteria = new LinkedHashMap<>();

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private JsonObject raw;

        /**
         * Reads an advancement from JSON
         * @param json the advancement's JSON
         * @return the advancement
         */
        public static Advancement fromJson(JsonObject json) {
            Advancement adv = new Advancement();
            adv.raw = RawJson.copy(json);

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

        /** {@return the advancement as JSON} */
        public JsonObject toJson() {
            JsonObject json = RawJson.copyOrNew(raw);
            json.put("done", done);

            JsonObject criteriaObj = new JsonObject();
            for (Map.Entry<String, String> entry : criteria.entrySet()) {
                criteriaObj.put(entry.getKey(), entry.getValue());
            }
            json.put("criteria", criteriaObj);

            return json;
        }

        /**
         * {@return when a criterion was done, or null if it wasn't}
         * @param criterionKey the criterion
         */
        @Nullable
        public String getCriterionDate(String criterionKey) {
            return criteria.get(criterionKey);
        }
    }
}