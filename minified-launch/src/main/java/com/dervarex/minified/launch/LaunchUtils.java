package com.dervarex.minified.launch;

import java.util.ArrayList;
import java.util.List;

public final class LaunchUtils {
    private LaunchUtils() {
    }

    public static String[] safeArgs(String[] args) {
        if (args == null || args.length == 0) {
            return new String[0];
        }
        List<String> cleaned = new ArrayList<>();
        for (String arg : args) {
            if (arg == null) {
                continue;
            }
            String trimmed = arg.trim();
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }
        return cleaned.toArray(new String[0]);
    }
}

// https://launchermeta.mojang.com/mc/game/version_manifest.json

/*
 * File file
 * -> convert to jsonfile
 * jsonfile. and here you have the objects, arrays and stuff
 * example:
 *
{
  "id": 10245,
  "username": "coder_99",
  "email": "coder_99@example.com",
  "is_active": true,
  "skills": ["Python", "JavaScript", "JSON"],
  "address": {
    "city": "Frankfurt",
    "country": "Germany"
  },
  "subscription_tier": null
}
 * JsonFile jsonfile = new JsonFile(file)
 * file.id / file.getId
 * file.address.city / file.getAddress().getCity()
 * (only use one system, eighter with getters or directly, whatever works better for you
 * support full json spec, including nested objects, arrays, nulls, booleans, numbers and strings
 *
 *
 *
 */