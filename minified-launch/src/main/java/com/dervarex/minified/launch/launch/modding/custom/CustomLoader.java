package com.dervarex.minified.launch.launch.modding.custom;

import com.dervarex.minified.launch.launch.modding.Loader;
import java.util.List;

/**
 * Custom Loader Option, the launcher will have to handle installation itself, we will not modify anything
 * @param mcVersion the base minecraft version
 * @param loaderVersion the custom loader version
 * @param iconUrl direct url to an icon for the loader
 * @param mainClass the mainclass to use to launch minecraft
 * @param customJvmArgs extra jvm args used to launch minecraft
 * @param customGameArgs extra game args used to launch minecraft
 * @param customClasspathEntries entries that will be added to the classpath on launch
 */
public record CustomLoader(
        String name,
        String mcVersion,
        String loaderVersion,
        String iconUrl,
        String mainClass,
        List<String> customJvmArgs,
        List<String> customGameArgs,
        List<String> customClasspathEntries
) implements Loader {

    public CustomLoader {
        customJvmArgs = customJvmArgs == null ? List.of() : List.copyOf(customJvmArgs);
        customGameArgs = customGameArgs == null ? List.of() : List.copyOf(customGameArgs);
        customClasspathEntries = customClasspathEntries == null ? List.of() : List.copyOf(customClasspathEntries);
    }
}