package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.utils.json.JsonFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class ForgeVersionJson {
    /**
     *
     * @param gameDir the game directory
     * @param loaderVersion the forge loader version, for example 1.21.11-61.1.8
     * @return the version JSON for the given forge loader version
     * @throws IOException if an I/O error occurs while fetching the version JSON
     */
    public static JsonFile getVersionJson(Path gameDir, String loaderVersion) throws IOException {
        String version = loaderVersion.split("-")[0];
        String loader = loaderVersion.split("-")[1];
        return new JsonFile(
                gameDir
                        .resolve("versions")
                        .resolve(version + "-forge-" + loader)
                        .resolve(version + "-forge-" + loader + ".json")
                        .toFile()
        );
    }
}
