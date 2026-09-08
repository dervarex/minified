package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class Stats {
    @Setter
    private int dataVersion;
    private final Map<String, Map<String, Integer>> categories = new LinkedHashMap<>();

    public static Stats fromJson(JsonObject json) {
        Stats result = new Stats();

        if (json.has("DataVersion")) {
            result.dataVersion = json.get("DataVersion").asInt();
        }

        if (json.has("stats")) {
            JsonValue statsVal = json.get("stats");
            if (statsVal != null && statsVal.isObject()) {
                JsonObject statsObj = statsVal.asObject();

                for (String categoryKey : statsObj.keys()) {
                    JsonValue categoryVal = statsObj.get(categoryKey);

                    if (categoryVal != null && categoryVal.isObject()) {
                        JsonObject categoryObj = categoryVal.asObject();
                        Map<String, Integer> entries = new LinkedHashMap<>();

                        for (String statKey : categoryObj.keys()) {
                            entries.put(statKey, categoryObj.get(statKey).asInt());
                        }

                        result.categories.put(categoryKey, entries);
                    }
                }
            }
        }

        return result;
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonObject statsObj = new JsonObject();

        for (Map.Entry<String, Map<String, Integer>> categoryEntry : categories.entrySet()) {
            JsonObject categoryObj = new JsonObject();

            for (Map.Entry<String, Integer> statEntry : categoryEntry.getValue().entrySet()) {
                categoryObj.put(statEntry.getKey(), statEntry.getValue());
            }

            statsObj.put(categoryEntry.getKey(), categoryObj);
        }

        root.put("stats", statsObj);
        root.put("DataVersion", dataVersion);

        return root;
    }

    public int getStat(String category, String statName) {
        Map<String, Integer> categoryMap = categories.get(category);
        if (categoryMap == null) {
            return 0;
        }
        return categoryMap.getOrDefault(statName, 0);
    }
}