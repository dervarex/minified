package com.dervarex.minified.launch;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class LaunchOptions {
    private final Map<String, String> variables =
            new HashMap<>();

    private final Map<String, Boolean> features =
            new HashMap<>();

    public LaunchOptions setVariable(
            String key,
            String value
    ) {
        variables.put(key, value);
        return this;
    }

    public LaunchOptions setFeature(
            String key,
            boolean value
    ) {
        features.put(key, value);
        return this;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public Map<String, Boolean> getFeatures() {
        return features;
    }

    public static LaunchOptions create() {
        return new LaunchOptions();
    }
}