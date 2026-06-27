package com.dervarex.minified.launch.launch.modding.neoforge;

import com.dervarex.minified.launch.launch.modding.Loader;

public record NeoforgeLoader(String mcVersion, String loaderVersion) implements Loader {
}