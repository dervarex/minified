package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.launch.modding.Loader;

public record QuiltLoader(String mcVersion, String loaderVersion) implements Loader {
}