package com.dervarex.minified.launch.arguments;

import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.util.ArrayList;

public final class JvmArgumentsParser {
    private static final String OS_NAME = getMinecraftOsName();
    private static final String OS_VERSION = System.getProperty("os.version");

    private JvmArgumentsParser() {
    }

    /**
     * Parses the JVM Arguments from the given JSON object, applying rules and replacing RAM placeholders with the provided values.
     * @param arguments the JSON object containing the JVM arguments, typically from the "default-user-jvm" field of a Minecraft version JSON
     * @param minRamMb minimum ram in megabytes
     * @param maxRamMb maximum ram in megabytes
     * @return an arraylist of jvm args to apply when starting the game
     */
    public static ArrayList<String> parse(
            JsonObject arguments,
            int minRamMb,
            int maxRamMb
    ) {
        ArrayList<String> jvmArgs = new ArrayList<>();

        JsonArray defaultUserJvm = arguments
                .get("default-user-jvm")
                .asArray();

        for (JsonValue entry : defaultUserJvm) {
            JsonObject object = entry.asObject();

            if (!isAllowed(object)) {
                continue;
            }

            JsonValue value = object.get("value");

            if (value.isString()) {
                addArg(
                        jvmArgs,
                        value.asString(),
                        minRamMb,
                        maxRamMb
                );
                continue;
            }

            for (JsonValue arg : value.asArray()) {
                addArg(
                        jvmArgs,
                        arg.asString(),
                        minRamMb,
                        maxRamMb
                );
            }
        }

        return jvmArgs;
    }
    /**
     * Parses the JVM Arguments from the given JSON object, applying rules and replacing RAM placeholders with the provided values.
     * @param jvmArray the JSON array containing the JVM arguments, typically from the "jvm" field of a Minecraft version JSON
     * @param minRamMb minimum ram in megabytes
     * @param maxRamMb maximum ram in megabytes
     * @return an arraylist of jvm args to apply when starting the game
     */

    public static ArrayList<String> parse(
            JsonArray jvmArray,
            int minRamMb,
            int maxRamMb
    ) {
        ArrayList<String> jvmArgs = new ArrayList<>();

        for (JsonValue entry : jvmArray) {
            // Handle direct string values (like "--sun-misc-unsafe-memory-access=allow")
            if (entry.isString()) {
                addArg(
                        jvmArgs,
                        entry.asString(),
                        minRamMb,
                        maxRamMb
                );
                continue;
            }

            // Handle object entries with rules and values
            if (!entry.isObject()) {
                continue;
            }

            JsonObject object = entry.asObject();

            if (!isAllowed(object)) {
                continue;
            }

            JsonValue value = object.get("value");

            if (value == null) {
                continue;
            }

            if (value.isString()) {
                addArg(
                        jvmArgs,
                        value.asString(),
                        minRamMb,
                        maxRamMb
                );
                continue;
            }

            if (value.isArray()) {
                for (JsonValue arg : value.asArray()) {
                    addArg(
                            jvmArgs,
                            arg.asString(),
                            minRamMb,
                            maxRamMb
                    );
                }
            }
        }

        return jvmArgs;
    }
    /**
     * Determines if the library is allowed on the users operating system
     * @param object the JsonObject of the library
     * @return true if the library is allowed to be downloaded on the users operating system, false otherwise
     */
    private static boolean isAllowed(JsonObject object) {
        JsonValue rulesValue = object.get("rules");

        if (rulesValue == null) {
            return true;
        }

        JsonArray rules = rulesValue.asArray();

        boolean allowed = false;

        for (JsonValue ruleValue : rules) {
            JsonObject rule = ruleValue.asObject();

            String action = rule.get("action").asString();

            JsonValue osValue = rule.get("os");

            boolean matches = true;

            if (osValue != null) {
                JsonObject os = osValue.asObject();

                JsonValue nameValue = os.get("name");

                if (nameValue != null) {
                    matches &= OS_NAME.equals(nameValue.asString());
                }

                JsonValue versionRangeValue = os.get("versionRange");

                if (versionRangeValue != null) {
                    JsonObject versionRange = versionRangeValue.asObject();

                    JsonValue minValue = versionRange.get("min");

                    if (minValue != null) {
                        matches &= compareVersions(
                                OS_VERSION,
                                minValue.asString()
                        ) >= 0;
                    }

                    JsonValue maxValue = versionRange.get("max");

                    if (maxValue != null) {
                        matches &= compareVersions(
                                OS_VERSION,
                                maxValue.asString()
                        ) <= 0;
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

    private static void addArg(
            ArrayList<String> args,
            String arg,
            int minRamMb,
            int maxRamMb
    ) {
        if (arg.startsWith("-Xms")) {
            args.add("-Xms" + minRamMb + "M");
            return;
        }

        if (arg.startsWith("-Xmx")) {
            args.add("-Xmx" + maxRamMb + "M");
            return;
        }

        args.add(arg);
    }

    /**
     * @return the operating system of the user, in the format that Minecraft uses for library rules
     */
    private static String getMinecraftOsName() {
        String os = System.getProperty("os.name").toLowerCase();

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

    private static int compareVersions(
            String current,
            String target
    ) {
        String[] currentParts = current.split("\\.");
        String[] targetParts = target.split("\\.");

        int length = Math.max(
                currentParts.length,
                targetParts.length
        );

        for (int i = 0; i < length; i++) {
            int currentPart =
                    i < currentParts.length
                            ? parseInt(currentParts[i])
                            : 0;

            int targetPart =
                    i < targetParts.length
                            ? parseInt(targetParts[i])
                            : 0;

            if (currentPart < targetPart) {
                return -1;
            }

            if (currentPart > targetPart) {
                return 1;
            }
        }

        return 0;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }
}