package com.dervarex.minified.launch.launch.modding.forge.installer;

import com.dervarex.minified.launch.exceptions.loader.forge.ForgePreparationException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.download.DownloadHelper;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonString;
import org.apiguardian.api.API;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Installs Forge up to 1.5.1. Forge was a jar mod back then, its zip got copied into minecraft.jar
 * (see {@link LegacyForgeGameJar}) <br>
 * FML downloads a few libraries into {@code <game directory>/lib} on its first start, from a server that is gone already
 * 1.5.x can be pointed at a mirror ({@code fml.core.libraries.mirror}), older versions can't
 */
@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public final class JarModForgeInstaller {

    // 1.3.2 - 1.5.1 ship a universal zip, 1.1 - 1.2.5 ships a client zip
    private static final List<String> JAR_MOD_CLASSIFIERS = List.of("universal", "client");

    private static final String FML_LIBRARIES_CLASS = "cpw/mods/fml/relauncher/CoreFMLLibraries.class";

    private JarModForgeInstaller() {
    }

    public static void install(LaunchConfiguration config) {
        String loaderVersion = config.getLoader().loaderVersion();
        Path gameDir = config.getJarFile().toAbsolutePath().getParent();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        try {
            Path jarMod = downloadJarMod(loaderVersion, gameDir, client);
            downloadFmlLibraries(jarMod, gameDir.resolve("lib"), client);
            writeProfile(config, gameDir, jarMod);
        } catch (IOException e) {
            throw new ForgePreparationException(e);
        }
    }

    private static Path downloadJarMod(String loaderVersion, Path gameDir, HttpClient client) throws IOException {
        for (String classifier : JAR_MOD_CLASSIFIERS) {
            String fileName = "forge-" + loaderVersion + "-" + classifier + ".zip";
            String url = ApiEndpoints.FORGE_INSTALLER_BASE_URL + loaderVersion + "/" + fileName;

            String sha1;
            try {
                sha1 = HttpUtil.get(url + ".sha1").trim().split("\\s+")[0];
            } catch (HttpException e) {
                if (e.getStatusCode() == 404) {
                    continue;
                }
                throw new IOException("Failed to fetch the checksum of " + url, e);
            }

            Path target = gameDir.resolve("libraries/net/minecraftforge/forge").resolve(loaderVersion).resolve(fileName);
            if (!DownloadHelper.download(url, target, sha1, client)) {
                throw new IOException("Checksum mismatch for " + url);
            }
            return target;
        }
        throw new IOException("Forge " + loaderVersion + " has neither an installer nor a universal or client zip");
    }
    private static void downloadFmlLibraries(Path jarMod, Path libDir, HttpClient client) throws IOException {
        for (String[] library : readFmlLibraries(jarMod)) {
            String url = ApiEndpoints.FML_LIBRARIES_MIRROR_URL.formatted(library[0]);
            if (!DownloadHelper.download(url, libDir.resolve(library[0]), library[1], client)) {
                throw new IOException("Checksum mismatch for " + url);
            }
        }
    }

    /**
     * Reads file names and checksums out of FML's CoreFMLLibraries class
     * Static initializer has them as string constants
     * I have no idea what the 1.5.x deobf shit does, something runtime, FML downloads that itself through the mirror or whatever
     *
     * @return {file name, sha1} pairs, empty if no FML or the class looks different
     */
    static List<String[]> readFmlLibraries(Path jarMod) throws IOException {
        List<String> strings;
        try (ZipFile zip = new ZipFile(jarMod.toFile())) {
            ZipEntry entry = zip.getEntry(FML_LIBRARIES_CLASS);
            if (entry == null) {
                return List.of();
            }
            try (InputStream in = zip.getInputStream(entry)) {
                strings = readStringConstants(in);
            }
        }

        List<String> names = new ArrayList<>();
        List<String> hashes = new ArrayList<>();
        for (String string : strings) {
            if (string.matches("[\\w.-]+\\.(jar|zip)")) {
                names.add(string);
            } else if (string.matches("[0-9a-f]{40}")) {
                hashes.add(string);
            }
        }

        if (names.size() != hashes.size()) {
            return List.of();
        }

        List<String[]> libraries = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            libraries.add(new String[]{names.get(i), hashes.get(i)});
        }
        return libraries;
    }

    /**
     * @return the CONSTANT_Utf8 entries of a class file's constant pool, in order
     */
    private static List<String> readStringConstants(InputStream classFile) throws IOException {
        DataInputStream in = new DataInputStream(classFile);
        in.readInt(); // magic
        in.readUnsignedShort(); // minor version
        in.readUnsignedShort(); // major version

        List<String> strings = new ArrayList<>();
        int count = in.readUnsignedShort();
        for (int i = 1; i < count; i++) {
            int tag = in.readUnsignedByte();
            switch (tag) {
                case 1 -> strings.add(in.readUTF());
                case 7, 8, 16, 19, 20 -> in.skipBytes(2);
                case 15 -> in.skipBytes(3);
                case 3, 4, 9, 10, 11, 12, 17, 18 -> in.skipBytes(4);
                case 5, 6 -> {
                    in.skipBytes(8);
                    i++; // longs and doubles take two slots
                }
                default -> throw new IOException("Unknown constant pool tag " + tag);
            }
        }
        return strings;
    }

    private static void writeProfile(LaunchConfiguration config, Path gameDir, Path jarMod) throws IOException {
        String loaderVersion = config.getLoader().loaderVersion();
        JsonFile vanilla = CacheManager.loadVersionJson(config.getLoader().mcVersion(), true);

        JsonArray jarMods = new JsonArray();
        jarMods.add(new JsonString(gameDir.relativize(jarMod).toString()));

        JsonObject profile = new JsonObject();
        profile.put("id", ForgeVersionJson.getVersionId(loaderVersion));
        profile.put("mainClass", vanilla.get("mainClass"));
        profile.put("minecraftArguments", vanilla.get("minecraftArguments"));
        profile.put("libraries", new JsonArray());
        profile.put(LegacyForgeGameJar.JAR_MODS_KEY, jarMods);

        Path profilePath = ForgeVersionJson.getVersionJsonPath(gameDir, loaderVersion);
        Files.createDirectories(profilePath.getParent());
        Files.writeString(profilePath, profile.toJson());
    }
}
