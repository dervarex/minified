package com.dervarex.minified.worlds.block;

import java.util.Map;

public record BlockState(String name, Map<String, String> properties) {
    public static BlockState of(String name) {
        return new BlockState(name, Map.of());
    }
}