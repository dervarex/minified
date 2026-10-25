package com.dervarex.minified.launch.arguments;

import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@API(status = API.Status.INTERNAL)
public final class LegacyMinecraftArgumentsParser {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    private LegacyMinecraftArgumentsParser() {
    }


    /**
     * Parses the Legacy Arguments
     * @param minecraftArguments minecraftArguments String from the JSON
     * @return List of the Game Arguments
     */
    public static List<String> parse(String minecraftArguments) {
        List<String> result = new ArrayList<>();

        if (minecraftArguments == null || minecraftArguments.isBlank()) {
            return result;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(minecraftArguments);
        while (matcher.find()) {
            String token = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (token != null && !token.isEmpty()) {
                result.add(token);
            }
        }

        return result;
    }
}