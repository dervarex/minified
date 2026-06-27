package com.dervarex.minified.launch.download.libraries;

import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.utils.OSUtil;
import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OfflineLibraryValidator {

    private OfflineLibraryValidator() {
    }

    public static void validate(String version, Loader loader, Path librariesDir) {
        List<String> problems = new ArrayList<>();
        Path cacheRoot = resolveCacheRoot(librariesDir);

        Path versionJsonPath = cacheRoot.resolve("versions").resolve(version + ".json");
        if (!Files.exists(versionJsonPath)) {
            throw new OfflineModeNeedsNetworkException("Missing cached version JSON: " + versionJsonPath);
        }

        JsonFile versionJson;
        try {
            versionJson = new JsonFile(Files.readString(versionJsonPath));
        } catch (Exception e) {
            throw new OfflineModeNeedsNetworkException("Failed to read cached version JSON: " + versionJsonPath);
        }

        JsonArray versionLibraries = versionJson.get("libraries").asArray();
        validateLibraries(versionLibraries, librariesDir, problems);

        switch (loader) {
            case FabricLoader ignored -> {
                Path fabricProfilePath = cacheRoot.resolve("profiles").resolve("fabric").resolve(version + ".json");
                validateLoaderProfile(fabricProfilePath, librariesDir, problems);
            }
            case QuiltLoader ignored -> {
                Path quiltProfilePath = cacheRoot.resolve("profiles").resolve("quilt").resolve(version + ".json");
                validateLoaderProfile(quiltProfilePath, librariesDir, problems);
            }
            case VanillaLoader ignored -> {
                // nothing extra here
            }
            case ForgeLoader ignored -> {
                // nothing extra here
            }
            case NeoforgeLoader ignored -> {
                // nothing extra here (why do I have to copy and paste this 3 times...)
            }
            default -> throw new IllegalStateException("Unexpected loader: " + loader);
        }

        if (!problems.isEmpty()) {
            throw new OfflineModeNeedsNetworkException(
                    "Offline launch blocked:\n- " + String.join("\n- ", problems)
            );
        }
    }

    private static void validateLoaderProfile(Path profilePath, Path librariesDir, List<String> problems) {
        if (!Files.exists(profilePath)) {
            problems.add("Missing cached loader profile: " + profilePath);
            return;
        }

        JsonFile profile;
        try {
            profile = new JsonFile(Files.readString(profilePath));
        } catch (Exception e) {
            problems.add("Invalid cached loader profile: " + profilePath);
            return;
        }

        JsonArray libraries = profile.get("libraries").asArray();
        validateLibraries(libraries, librariesDir, problems);
    }

    private static void validateLibraries(JsonArray libraries, Path librariesDir, List<String> problems) {
        for (JsonValue libraryValue : libraries) {
            JsonObject library = libraryValue.asObject();

            if (!isAllowed(library)) {
                continue;
            }

            JsonValue downloadsValue = library.get("downloads");
            if (downloadsValue == null) {
                continue;
            }

            JsonObject downloads = downloadsValue.asObject();

            JsonValue artifactValue = downloads.get("artifact");
            if (artifactValue != null) {
                JsonObject artifact = artifactValue.asObject();
                Path artifactPath = librariesDir.resolve(artifact.get("path").asString());
                String expectedSha1 = artifact.has("sha1") ? artifact.get("sha1").asString() : null;

                validateFile(
                        artifactPath,
                        expectedSha1,
                        "Missing or broken library: " + artifactPath,
                        problems
                );
            }

            NativeDownload nativeDownload = resolveNativeDownload(library, downloads);
            if (nativeDownload != null) {
                Path nativesDir = resolveNativesDirectory(librariesDir);
                Path archivePath = nativesDir.resolve(".downloads").resolve(nativeDownload.relativePath());

                validateFile(
                        archivePath,
                        nativeDownload.sha1(),
                        "Missing or broken native archive: " + archivePath,
                        problems
                );
            }
        }
    }

    private static void validateFile(
            Path file,
            String expectedSha1,
            String errorMessage,
            List<String> problems
    ) {
        if (!Files.exists(file) || !Files.isRegularFile(file) || isEmpty(file)) {
            problems.add(errorMessage);
            return;
        }

        if (expectedSha1 == null || expectedSha1.isBlank()) {
            return;
        }

        try {
            String actualSha1 = sha1(file);
            if (!actualSha1.equalsIgnoreCase(expectedSha1)) {
                problems.add("SHA1 mismatch: " + file);
            }
        } catch (Exception e) {
            problems.add("Failed to hash file: " + file);
        }
    }

    private static boolean isEmpty(Path file) {
        try {
            return Files.size(file) == 0;
        } catch (IOException e) {
            return true;
        }
    }

    private static String sha1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] bytes = Files.readAllBytes(file);
        byte[] hash = digest.digest(bytes);

        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(Character.forDigit((b >>> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static NativeDownload resolveNativeDownload(JsonObject library, JsonObject downloads) {
        JsonValue nativesValue = library.get("natives");
        JsonValue classifiersValue = downloads.get("classifiers");

        if (nativesValue == null || classifiersValue == null) {
            return null;
        }

        JsonObject natives = nativesValue.asObject();
        String os = getMinecraftOs();
        JsonValue classifierNameValue = natives.get(os);
        if (classifierNameValue == null) {
            return null;
        }

        String classifierName = classifierNameValue.asString();
        JsonObject classifiers = classifiersValue.asObject();
        JsonValue classifierValue = classifiers.get(classifierName);
        if (classifierValue == null) {
            return null;
        }

        JsonObject classifier = classifierValue.asObject();
        JsonValue pathValue = classifier.get("path");
        JsonValue sha1Value = classifier.get("sha1");

        if (pathValue == null || sha1Value == null) {
            return null;
        }

        return new NativeDownload(pathValue.asString(), sha1Value.asString());
    }

    private static boolean isAllowed(JsonObject library) {
        if (!library.has("rules")) {
            return true;
        }

        JsonArray rules = library.get("rules").asArray();
        String os = getMinecraftOs();
        boolean allowed = false;

        for (JsonValue ruleValue : rules) {
            JsonObject rule = ruleValue.asObject();
            String action = rule.get("action").asString();

            if (!rule.has("os")) {
                allowed = action.equals("allow");
                continue;
            }

            JsonObject osObject = rule.get("os").asObject();
            String ruleOs = osObject.get("name").asString();

            if (ruleOs.equals(os)) {
                allowed = action.equals("allow");
            }
        }

        return allowed;
    }

    private static String getMinecraftOs() {
        return OSUtil.getMinecraftOs();
    }

    private static Path resolveNativesDirectory(Path librariesDir) {
        Path parent = librariesDir.toAbsolutePath().getParent();
        if (parent == null) {
            return librariesDir.toAbsolutePath().resolve("natives");
        }
        return parent.resolve("jar").resolve("natives").toAbsolutePath();
    }

    private record NativeDownload(String relativePath, String sha1) {
    }

    private static Path resolveCacheRoot(Path librariesDir) {
        Path parent = librariesDir.toAbsolutePath().getParent();
        return Objects.requireNonNullElseGet(parent, librariesDir::toAbsolutePath).resolve("cache");
    }
}