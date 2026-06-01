package com.dervarex.minified.launch.arguments;

import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonBoolean;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonString;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameArgumentsParserTest {

    @Test
    void parseReplacesVariablesInPlainStringEntries() {
        JsonArray arguments = new JsonArray();
        arguments.add(new JsonString("--username"));
        arguments.add(new JsonString("${auth_player_name}"));

        List<String> parsed = GameArgumentsParser.parse(
                arguments,
                Map.of("auth_player_name", "Alex"),
                Map.of()
        );

        assertEquals(List.of("--username", "Alex"), parsed);
    }

    @Test
    void parseIncludesFeatureGatedValuesWhenFeatureMatches() {
        JsonArray rules = new JsonArray();
        rules.add(new JsonObject(Map.of(
                "action", new JsonString("allow"),
                "features", new JsonObject(Map.of("has_custom_resolution", new JsonBoolean(true)))
        )));

        JsonArray value = new JsonArray();
        value.add(new JsonString("--width"));
        value.add(new JsonString("${resolution_width}"));

        JsonArray arguments = new JsonArray();
        arguments.add(new JsonObject(Map.of(
                "rules", rules,
                "value", value
        )));

        List<String> parsed = GameArgumentsParser.parse(
                arguments,
                Map.of("resolution_width", "1920"),
                Map.of("has_custom_resolution", true)
        );

        assertEquals(List.of("--width", "1920"), parsed);
    }

    @Test
    void parseSkipsFeatureGatedValuesWhenFeatureDoesNotMatch() {
        JsonArray rules = new JsonArray();
        rules.add(new JsonObject(Map.of(
                "action", new JsonString("allow"),
                "features", new JsonObject(Map.of("is_demo_user", new JsonBoolean(true)))
        )));

        JsonArray arguments = new JsonArray();
        arguments.add(new JsonObject(Map.of(
                "rules", rules,
                "value", new JsonString("--demo")
        )));

        List<String> parsed = GameArgumentsParser.parse(
                arguments,
                Map.of(),
                Map.of("is_demo_user", false)
        );

        assertEquals(List.of(), parsed);
    }

    @Test
    void parseHonorsOsRules() {
        JsonArray rules = new JsonArray();
        rules.add(new JsonObject(Map.of(
                "action", new JsonString("allow"),
                "os", new JsonObject(Map.of("name", new JsonString(currentOsToken())))
        )));

        JsonArray arguments = new JsonArray();
        arguments.add(new JsonObject(Map.of(
                "rules", rules,
                "value", new JsonString("--supported-os")
        )));

        List<String> parsed = GameArgumentsParser.parse(arguments, Map.of(), Map.of());

        assertEquals(List.of("--supported-os"), parsed);
    }

    private static String currentOsToken() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("win")) {
            return "windows";
        }
        if (osName.contains("mac")) {
            return "osx";
        }
        if (osName.contains("linux")) {
            return "linux";
        }
        return "unknown";
    }
}

