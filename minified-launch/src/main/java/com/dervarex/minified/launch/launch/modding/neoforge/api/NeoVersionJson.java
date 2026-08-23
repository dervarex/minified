package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.apiguardian.api.API;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class NeoVersionJson {
    private NeoVersionJson() {
    }

    /**
     * Resolves the local NeoForge version JSON as automatically as possible.
     */
    @API(status = API.Status.STABLE)
    public static JsonFile getVersionJson(Path gameDir, String loaderVersion) throws IOException {
        Path versionsDir = gameDir.resolve("versions");

        for (Path candidate : candidatePaths(versionsDir, loaderVersion)) {
            if (Files.exists(candidate)) {
                return new JsonFile(candidate.toFile());
            }
        }

        Path found = findByScanning(versionsDir, loaderVersion);
        if (found != null) {
            return new JsonFile(found.toFile());
        }

        Path fallbackFolder = versionsDir.resolve("neoforge-" + loaderVersion);
        return new JsonFile(
                fallbackFolder.resolve("neoforge-" + loaderVersion + ".json").toFile()
        );
    }

    private static List<Path> candidatePaths(Path versionsDir, String loaderVersion) {
        List<Path> paths = new ArrayList<>();

        paths.add(
                versionsDir
                        .resolve(loaderVersion)
                        .resolve(loaderVersion + ".json")
        );

        paths.add(
                versionsDir
                        .resolve("neoforge-" + loaderVersion)
                        .resolve("neoforge-" + loaderVersion + ".json")
        );

        paths.add(
                versionsDir
                        .resolve("neoforge-" + loaderVersion)
                        .resolve(loaderVersion + ".json")
        );

        return paths;
    }

    private static Path findByScanning(Path versionsDir, String loaderVersion) throws IOException {
        if (!Files.isDirectory(versionsDir)) {
            return null;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(versionsDir)) {
            for (Path versionFolder : stream) {
                if (!Files.isDirectory(versionFolder)) {
                    continue;
                }

                String folderName = versionFolder.getFileName().toString();
                Path jsonFile = versionFolder.resolve(folderName + ".json");

                if (!Files.exists(jsonFile)) {
                    continue;
                }

                try {
                    JsonObject root = JsonParser
                            .parse(Files.readString(jsonFile))
                            .asObject();

                    if (!root.containsKey("id")) {
                        continue;
                    }

                    String id = root.get("id").asString();

                    if (id.equals("neoforge-" + loaderVersion)) {
                        return jsonFile;
                    }

                } catch (Exception e) {
                    // Ignore invalid version jsons
                }
            }
        }

        return null;
    }
}