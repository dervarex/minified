package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.utils.json.JsonFile;
import org.apiguardian.api.API;

import java.io.IOException;
import java.nio.file.Path;

public class ForgeVersionJson {
    /**
     *
     * @param gameDir the game directory
     * @param loaderVersion the forge loader version, for example 1.21.11-61.1.8
     * @return the version JSON for the given forge loader version
     * @throws IOException if an I/O error occurs while fetching the version JSON
     */
    @API(status = API.Status.STABLE)
    public static JsonFile getVersionJson(Path gameDir, String loaderVersion) throws IOException {
        return new JsonFile(
                getVersionJsonPath(gameDir, loaderVersion).toFile()
        );
    }

    /**
     * @param gameDir the game directory
     * @param loaderVersion the forge loader version, for example {@code 1.21.11-61.1.8} or {@code 1.7.10-10.13.4.1614-1.7.10}
     * @return where the installer puts the version JSON, e.g. {@code versions/1.21.11-forge-61.1.8/1.21.11-forge-61.1.8.json}
     */
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static Path getVersionJsonPath(Path gameDir, String loaderVersion) {
        String id = getVersionId(loaderVersion);
        return gameDir
                .resolve("versions")
                .resolve(id)
                .resolve(id + ".json");
    }

    /**
     * @param loaderVersion the forge loader version, for example 1.21.11-61.1.8
     * @return the id of the installed version, e.g. {@code 1.21.11-forge-61.1.8}
     */
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static String getVersionId(String loaderVersion) {
        int separator = loaderVersion.indexOf('-');
        String version = loaderVersion.substring(0, separator);
        // old versions have a suffix (1.7.10-10.13.4.1614-1.7.10)
        String loader = loaderVersion.substring(separator + 1);
        return version + "-forge-" + loader;
    }
}
