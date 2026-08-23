package com.dervarex.minified.launch.arguments;

import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GameArgumentsParser {
    private static final String OS_NAME = getMinecraftOsName();

    private GameArgumentsParser() {
    }

    public static List<String> parse(
            JsonArray arguments,
            Map<String, String> variables,
            Map<String, Boolean> features
    ) {
        ArrayList<String> output = new ArrayList<>();
        boolean[] skipNext = new boolean[] { false };

        for (JsonValue entry : arguments) {

            if (entry.isString()) {
                appendArgument(
                        output,
                        replaceVariables(entry.asString(), variables),
                        skipNext
                );
                continue;
            }

            JsonObject object = entry.asObject();

            if (!isAllowed(object, features)) {
                continue;
            }

            JsonValue value = object.get("value");

            if (value.isString()) {
                appendArgument(
                        output,
                        replaceVariables(value.asString(), variables),
                        skipNext
                );
                continue;
            }

            for (JsonValue arg : value.asArray()) {
                appendArgument(
                        output,
                        replaceVariables(arg.asString(), variables),
                        skipNext
                );
            }
        }

        return output;
    }
    /**
     * Determines if the library is allowed on the users operating system
     * @param object the JsonObject of the library
     * @param features the features map to check for feature rules
     * @return true if the library is allowed to be downloaded on the users operating system, false otherwise
     */
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

    /**
     * @param input the input with the variable placeholders
     * @param variables variables to replace in the input, where the key is the variable name and the value is the variable value
     * @return the input with the variable placeholders replaced with their corresponding values from the variables map
     */
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


    /**
     * @return the operating system of the user, in the format that Minecraft uses for library rules
     */
    private static String getMinecraftOsName() {
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
    private static void appendArgument(
            List<String> output,
            String arg,
            boolean[] skipNext
    ) {
        if (skipNext[0]) {
            skipNext[0] = false;
            return;
        }

        if ("--clientId".equals(arg)) {
            skipNext[0] = true;
            return;
        }
        if ("--xuid".equals(arg)) {
            skipNext[0] = true;
            return;
        }

        output.add(arg);
    }
}