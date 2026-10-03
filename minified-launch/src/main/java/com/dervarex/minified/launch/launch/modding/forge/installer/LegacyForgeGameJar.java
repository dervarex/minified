package com.dervarex.minified.launch.launch.modding.forge.installer;

import com.dervarex.minified.launch.exceptions.loader.forge.ForgePreparationException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Forge up to 1.5.2 changes the Minecraft classes themselves, which fails with the signed vanilla jar
 * So these versions get their own game jar without the signature, with the mods of Forge copied over it
 */
@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public final class LegacyForgeGameJar {

    /**
     * Set on the version JSON for Forge up to 1.5.1, the relative paths of the zips that have to be copied into the game jar
     */
    public static final String JAR_MODS_KEY = "minified:jarMods";

    private LegacyForgeGameJar() {
    }

    /**
     * @param loaderProfile the Forge version JSON
     * @return the game jar to use instead of the vanilla one, or null if the vanilla one works
     */
    public static Path resolve(JsonObject loaderProfile, LaunchConfiguration config) {
        if (!needsOwnGameJar(loaderProfile)) {
            return null;
        }
        Path versionJson = ForgeVersionJson.getVersionJsonPath(
                config.getJarFile().toAbsolutePath().getParent(),
                config.getLoader().loaderVersion()
        );
        return versionJson.resolveSibling(versionJson.getParent().getFileName() + ".jar");
    }

    /**
     * Builds the game jar returned by {@link #resolve}, if the version needs one. Has to run after the vanilla jar was downloaded
     */
    public static void prepare(JsonObject loaderProfile, LaunchConfiguration config) {
        Path gameJar = resolve(loaderProfile, config);
        if (gameJar == null) {
            return;
        }

        Path vanillaJar = config.getJarFile().toAbsolutePath();
        List<Path> jarMods = jarMods(loaderProfile, vanillaJar.getParent());

        try {
            if (isUpToDate(gameJar, vanillaJar, jarMods)) {
                return;
            }

            Path tempJar = gameJar.resolveSibling(gameJar.getFileName() + ".tmp");
            Files.createDirectories(gameJar.getParent());

            try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(tempJar))) {
                Set<String> written = new HashSet<>();
                // the last jar mod wins
                for (int i = jarMods.size() - 1; i >= 0; i--) {
                    copyEntries(jarMods.get(i), out, written);
                }
                copyEntries(vanillaJar, out, written);
            }

            Files.move(tempJar, gameJar, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ForgePreparationException(e);
        }
    }

    private static boolean needsOwnGameJar(JsonObject loaderProfile) {
        Boolean stripMeta = loaderProfile.getBoolean(LegacyForgeInstaller.STRIP_META_KEY);
        return (stripMeta != null && stripMeta) || loaderProfile.has(JAR_MODS_KEY);
    }

    private static List<Path> jarMods(JsonObject loaderProfile, Path gameDir) {
        List<Path> jarMods = new ArrayList<>();
        if (loaderProfile.has(JAR_MODS_KEY)) {
            for (JsonValue jarMod : loaderProfile.get(JAR_MODS_KEY).asArray()) {
                // relative to the game directory
                jarMods.add(gameDir.resolve(jarMod.asString()));
            }
        }
        return jarMods;
    }

    private static boolean isUpToDate(Path gameJar, Path vanillaJar, List<Path> jarMods) throws IOException {
        if (!Files.exists(gameJar)) {
            return false;
        }
        long builtAt = Files.getLastModifiedTime(gameJar).toMillis();
        if (Files.getLastModifiedTime(vanillaJar).toMillis() > builtAt) {
            return false;
        }
        for (Path jarMod : jarMods) {
            if (Files.getLastModifiedTime(jarMod).toMillis() > builtAt) {
                return false;
            }
        }
        return true;
    }

    private static void copyEntries(Path zipPath, ZipOutputStream out, Set<String> written) throws IOException {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                // META-INF holds the signature
                if (entry.isDirectory() || name.startsWith("META-INF/") || !written.add(name)) {
                    continue;
                }
                out.putNextEntry(new ZipEntry(name));
                try (InputStream in = zip.getInputStream(entry)) {
                    in.transferTo(out);
                }
                out.closeEntry();
            }
        }
    }
}
