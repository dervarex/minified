package com.dervarex.minified.launch.arguments;

import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class GameArgumentsParser {
    private static final String OS_NAME = getOsName();

    private GameArgumentsParser() {
    }

    public static List<String> parse(
            JsonArray arguments,
            Map<String, String> variables,
            Map<String, Boolean> features
    ) {
        ArrayList<String> output = new ArrayList<>();

        for (JsonValue entry : arguments) {

            if (entry.isString()) {

                output.add(
                        replaceVariables(
                                entry.asString(),
                                variables
                        )
                );

                continue;
            }

            JsonObject object = entry.asObject();

            if (!isAllowed(object, features)) {
                continue;
            }

            JsonValue value = object.get("value");

            if (value.isString()) {

                output.add(
                        replaceVariables(
                                value.asString(),
                                variables
                        )
                );

                continue;
            }

            for (JsonValue arg : value.asArray()) {

                output.add(
                        replaceVariables(
                                arg.asString(),
                                variables
                        )
                );
            }
        }

        return output;
    }

    private static boolean isAllowed(
            JsonObject object,
            Map<String, Boolean> features
    ) {
        JsonValue rulesValue = object.get("rules");

        if (rulesValue == null) {
            return true;
        }

        JsonArray rules = rulesValue.asArray();

        boolean allowed = false;

        for (JsonValue ruleValue : rules) {

            JsonObject rule = ruleValue.asObject();

            String action =
                    rule.get("action").asString();

            boolean matches = true;

            JsonValue osValue = rule.get("os");

            if (osValue != null) {

                JsonObject os = osValue.asObject();

                JsonValue nameValue = os.get("name");

                if (nameValue != null) {

                    matches &= OS_NAME.equals(
                            nameValue.asString()
                    );
                }
            }

            JsonValue featuresValue =
                    rule.get("features");

            if (featuresValue != null) {

                JsonObject featureObject =
                        featuresValue.asObject();

                for (String featureName :
                        featureObject.keys()) {

                    boolean required =
                            featureObject
                                    .get(featureName)
                                    .asBoolean();

                    boolean current =
                            features.getOrDefault(
                                    featureName,
                                    false
                            );

                    if (required != current) {
                        matches = false;
                        break;
                    }
                }
            }

            if (!matches) {
                continue;
            }

            if (action.equals("allow")) {
                allowed = true;
            }

            if (action.equals("disallow")) {
                allowed = false;
            }
        }

        return allowed;
    }

    private static String replaceVariables(
            String input,
            Map<String, String> variables
    ) {
        String result = input;

        for (String key : variables.keySet()) {

            String value =
                    variables.get(key);

            if (value == null) {
                value = "";
            }

            result = result.replace(
                    "${" + key + "}",
                    value
            );
        }

        return result;
    }

    private static String getOsName() {
        String os =
                System.getProperty("os.name")
                        .toLowerCase();

        if (os.contains("win")) {
            return "windows";
        }

        if (os.contains("mac")) {
            return "osx";
        }

        if (os.contains("linux")) {
            return "linux";
        }

        return "unknown";
    }
}