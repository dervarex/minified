package com.dervarex.minified.launch.launch.modding.forge;

import com.dervarex.minified.launch.launch.modding.Loader;

public record ForgeLoader(String mcVersion, String loaderVersion) implements Loader {
}