package com.dervarex.minified.launch.launch.modding.forge.installer;

import com.dervarex.minified.launch.exceptions.loader.forge.ForgePreparationException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.download.DownloadHelper;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Installs Forge for 1.5.2 - 1.12.2 (installer v1). Those installers can't be run without their GUI,
 * but all they do is extract the universal jar and write {@code versionInfo} as the version JSON, so we do that without it <br>
 * Unlike the new installer, they don't download the libraries either, the old launcher did that
 */
@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public final class LegacyForgeInstaller {

    /**
     * Set on the version JSON if the game jar has to be copied without its signature (1.5.2), see {@link LegacyForgeGameJar}
     */
    public static final String STRIP_META_KEY = "minified:stripMeta";

    private static final String MINECRAFT_LIBRARIES_URL = "https://libraries.minecraft.net/";

    private LegacyForgeInstaller() {
    }

    /**
     * @param installer the downloaded installer jar
     * @return true if the installer is a legacy (v1) installer
     */
    public static boolean isLegacyInstaller(Path installer) {
        JsonObject profile = readInstallProfile(installer);
        return profile != null && profile.has("install") && profile.has("versionInfo");
    }

    public static void install(Path installer, LaunchConfiguration config) {
        JsonObject profile = readInstallProfile(installer);
        if (profile == null) {
            throw new ForgePreparationException(new IOException("No install_profile.json in " + installer));
        }

        JsonObject install = profile.get("install").asObject();
        JsonObject versionInfo = profile.get("versionInfo").asObject();
        Path gameDir = config.getJarFile().toAbsolutePath().getParent();
        Path librariesDir = gameDir.resolve("libraries");

        try {
            Path universal = librariesDir.resolve(toArtifactPath(install.get("path").asString()));
            extractEntry(installer, install.get("filePath").asString(), universal);

            Boolean stripMeta = install.getBoolean("stripMeta");
            if (stripMeta != null && stripMeta) {
                versionInfo.put(STRIP_META_KEY, true);
            }

            Path versionJson = ForgeVersionJson.getVersionJsonPath(gameDir, config.getLoader().loaderVersion());
            Files.createDirectories(versionJson.getParent());
            Files.writeString(versionJson, versionInfo.toJson());

            downloadLibraries(versionInfo.get("libraries").asArray(), librariesDir, config, universal);
        } catch (IOException e) {
            throw new ForgePreparationException(e);
        }
    }

    private static void downloadLibraries(JsonArray libraries, Path librariesDir, LaunchConfiguration config, Path universal)
            throws IOException {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        for (JsonValue value : libraries) {
            JsonObject library = value.asObject();
            Boolean clientreq = library.getBoolean("clientreq");

            // natives come from the vanilla version JSON
            if (library.has("natives") || (clientreq != null && !clientreq)) {
                continue;
            }

            String artifactPath = toArtifactPath(library.get("name").asString());
            Path target = librariesDir.resolve(artifactPath);
            if (target.equals(universal)
                    || Files.exists(config.getLibrariesDirectory().toAbsolutePath().resolve(artifactPath))) {
                continue;
            }

            String baseUrl = library.has("url") ? library.get("url").asString() : MINECRAFT_LIBRARIES_URL;
            // files.minecraftforge.net/maven redirects to some http thing, which the new maven doesn't serve anymore
            baseUrl = baseUrl.replace("http://files.minecraftforge.net/maven/", "https://maven.minecraftforge.net/");
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }

            String url = baseUrl + artifactPath;
            if (!download(url, target, checksums(url, library), client)) {
                throw new IOException("Checksum mismatch for " + url);
            }
        }
    }

    private static boolean download(String url, Path target, List<String> checksums, HttpClient client) throws IOException {
        for (String sha1 : checksums) {
            if (DownloadHelper.download(url, target, sha1, client)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the checksums the library may have, the {@code .sha1} file of the maven if there is one,
     * otherwise the ones in the version JSON (they also contain the checksum of the .pack.xz variant)
     */
    private static List<String> checksums(String url, JsonObject library) throws IOException {
        try {
            return List.of(HttpUtil.get(url + ".sha1").trim().split("\\s+")[0]);
        } catch (Exception e) {
            // the forge maven lost some .sha1 files, e.g. scala-parser-combinators_2.11 1.0.1
            if (!library.has("checksums")) {
                throw new IOException("Failed to fetch the checksum of " + url, e);
            }
        }

        List<String> checksums = new ArrayList<>();
        for (JsonValue checksum : library.get("checksums").asArray()) {
            checksums.add(checksum.asString());
        }
        return checksums;
    }

    private static JsonObject readInstallProfile(Path installer) {
        try (ZipFile zip = new ZipFile(installer.toFile())) {
            ZipEntry entry = zip.getEntry("install_profile.json");
            if (entry == null) {
                return null;
            }
            try (InputStream in = zip.getInputStream(entry)) {
                return new JsonFile(new String(in.readAllBytes(), StandardCharsets.UTF_8)).asObject();
            }
        } catch (IOException e) {
            throw new ForgePreparationException(e);
        }
    }

    private static void extractEntry(Path zipPath, String entryName, Path target) throws IOException {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                throw new IOException("No " + entryName + " in " + zipPath);
            }
            if (Files.exists(target) && Files.size(target) == entry.getSize()) {
                return;
            }
            Files.createDirectories(target.getParent());
            try (InputStream in = zip.getInputStream(entry)) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * @param name maven coordinates, e.g. {@code net.minecraftforge:forge:1.7.10-10.13.4.1614-1.7.10}
     * @return the path of the jar in a maven repository
     */
    static String toArtifactPath(String name) {
        String[] parts = name.split(":");
        String fileName = parts[1] + "-" + parts[2] + (parts.length > 3 ? "-" + parts[3] : "") + ".jar";
        return parts[0].replace('.', '/') + "/" + parts[1] + "/" + parts[2] + "/" + fileName;
    }
}
