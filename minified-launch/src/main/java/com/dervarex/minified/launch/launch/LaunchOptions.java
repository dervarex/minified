package com.dervarex.minified.launch.launch;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @hidden to not confuse the user, they should use {@link LaunchConfigurator} instead,
 * this is just a wrapper for the launch options that are passed to the launch process.
 */
@Getter
@SuppressWarnings("unused")
final class LaunchOptions {
    private final Map<String, String> variables =
            new HashMap<>();

    private final Map<String, Boolean> features =
            new HashMap<>();

    LaunchOptions setVariable(
            String key,
            String value
    ) {
        variables.put(key, value);
        return this;
    }

    LaunchOptions setFeature(
            String key,
            boolean value
    ) {
        features.put(key, value);
        return this;
    }

    static LaunchOptions create() {
        return new LaunchOptions();
    }
}