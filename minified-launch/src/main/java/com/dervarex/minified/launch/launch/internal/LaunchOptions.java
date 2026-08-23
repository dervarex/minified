package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.utils.json.JsonFile;
import lombok.Getter;
import org.apiguardian.api.API;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * wrapper for the launch options that are passed to the launch process.
 */
@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
@Getter
public final class LaunchOptions {
    private final Map<String, String> variables =
            new HashMap<>();

    private final Map<String, Boolean> features =
            new HashMap<>();

    LaunchOptions setVariable(
            String key,
            String value
    ) {
        variables.put(key, value);
        return this;
    }

    LaunchOptions setFeature(
            String key,
            boolean value
    ) {
        features.put(key, value);
        return this;
    }

    static LaunchOptions create() {
        return new LaunchOptions();
    }

    public static LaunchOptions buildLaunchOptions(User user, String version, LaunchConfiguration launchConfig, JsonFile versionJson, String classpathString) {
        return
                LaunchOptions.create()

                        .setVariable(
                                "auth_player_name",
                                user == null ? launchConfig.getOfflineUsername() : user.username()
                        )

                        .setVariable(
                                "version_name",
                                version
                        )

                        .setVariable(
                                "game_directory",
                                launchConfig.getJarFile().getParent()
                                        .toAbsolutePath()
                                        .toString()
                        )

                        .setVariable(
                                "assets_root",
                                launchConfig.getAssetsDirectory()
                                        .toAbsolutePath()
                                        .toString()
                        )

                        .setVariable(
                                "assets_index_name",
                                versionJson.get("assetIndex") != null && versionJson.get("assetIndex").asObject().get("id") != null
                                        ? versionJson.get("assetIndex").asObject().get("id").asString()
                                        : ""
                        )

                        .setVariable(
                                "auth_uuid",
                                user == null ? getOfflineUuid(launchConfig.getOfflineUsername()) : user.uuid()
                        )

                        .setVariable(
                                "auth_access_token",
                                user == null ? "0" : user.accessToken()
                        )
                        .setVariable(
                                "user_properties",
                                "{}"
                        )

                        .setVariable(
                                "user_type",
                                user == null ? "legacy" : "msa"
                        )
                        .setVariable(
                                "game_assets",
                                launchConfig.getAssetsDirectory()
                                        .toAbsolutePath()
                                        .toString()
                        )

                        .setVariable(
                                "version_type",
                                versionJson.get("type") != null ? versionJson.get("type").asString() : "release"
                        )

                        .setVariable(
                                "resolution_width",
                                String.valueOf(launchConfig.getResolutionWidth())
                        )

                        .setVariable(
                                "resolution_height",
                                String.valueOf(launchConfig.getResolutionHeight())
                        )
                        .setVariable(
                                "library_directory",
                                launchConfig.getLibrariesDirectory()
                                        .toAbsolutePath()
                                        .toString()
                        )

                        .setVariable(
                                "launcher_name",
                                String.valueOf(launchConfig.getLauncherName())
                        )

                        .setVariable(
                                "launcher_version",
                                String.valueOf(launchConfig.getLauncherVersion())
                        )

                        .setVariable(
                                "classpath",
                                classpathString
                        )

                        .setVariable(
                                "natives_directory",
                                launchConfig.getNativesDirectory() != null ?
                                        launchConfig.getNativesDirectory().toAbsolutePath().toString() :
                                        launchConfig.getJarFile().getParent()
                                        .resolve("natives")
                                        .toAbsolutePath()
                                        .toString()
                        )
//                        .setVariable(
//                                "xuid",
//                                user == null ? "" : (user.getXuid() != null ? user.getXuid() : "")
//                        )

                        .setFeature(
                                "has_custom_resolution",
                                launchConfig.isCustomResolution()
                        )

                        .setFeature(
                                "is_demo_user",
                                launchConfig.isDemoUser()
                        );
    }
    private static String getOfflineUuid(String username) {
        return UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + username)
                        .getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}