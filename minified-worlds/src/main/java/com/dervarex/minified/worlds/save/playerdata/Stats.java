package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apiguardian.api.API;

/**
 * The statistics of a player, from stats/&lt;uuid&gt;.json
 */
@Getter
@API(status = API.Status.STABLE)
public class Stats {
    /**
     * Creates empty stats
     */
    public Stats() {
    }

    /** The data version of the Minecraft version that wrote it */
    @Setter
    private int dataVersion;
    /** The stats by category, then by id */
    private final Map<String, Map<String, Integer>> categories = new LinkedHashMap<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private JsonObject raw;

    /**
     * Reads the statistics from JSON
     * @param json the content of the player's stats file
     * @return the statistics
     */
    public static Stats fromJson(JsonObject json) {
        Stats result = new Stats();
        result.raw = RawJson.copy(json);

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

    /** {@return the statistics as JSON, like in the stats file} */
    public JsonObject toJson() {
        JsonObject root = RawJson.copyOrNew(raw);
        JsonValue existing = root.get("stats");
        JsonObject statsObj = existing != null && existing.isObject() ? existing.asObject() : new JsonObject();
        RawJson.removeMissingObjects(statsObj, categories.keySet());

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

    /**
     * {@return a statistic, 0 if the player doesn't have it}
     * @param category the category, like minecraft:mined
     * @param statName the statistic
     */
    public int getStat(String category, String statName) {
        Map<String, Integer> categoryMap = categories.get(category);
        if (categoryMap == null) {
            return 0;
        }
        return categoryMap.getOrDefault(statName, 0);
    }
}