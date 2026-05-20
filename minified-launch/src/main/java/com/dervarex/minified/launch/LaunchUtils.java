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

