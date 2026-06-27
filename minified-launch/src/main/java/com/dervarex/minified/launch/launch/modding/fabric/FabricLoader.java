package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.launch.modding.Loader;

public record FabricLoader(String mcVersion, String loaderVersion) implements Loader {
}