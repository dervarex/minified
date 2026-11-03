package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;

import java.time.Instant;

/**
 * A Minecraft version which Modrinth knows
 */
public final class GameVersion {
    private final String version;
    private final String versionType;
    private final Instant date;
    private final boolean major;

    GameVersion(JsonObject json) {
        version = ModrinthJson.string(json, "version");
        versionType = ModrinthJson.string(json, "version_type");
        date = ModrinthJson.instant(json, "date");
        major = ModrinthJson.bool(json, "major");
    }

    /** {@return the version, like {@code 1.21.11}} */
    public String getVersion() {
        return version;
    }

    /** {@return {@code release}, {@code snapshot}, {@code beta} or {@code alpha}} */
    public String getVersionType() {
        return versionType;
    }

    /** {@return when the version came out} */
    public Instant getDate() {
        return date;
    }

    /** {@return whether this is a major version, like {@code 1.21}} */
    public boolean isMajor() {
        return major;
    }

    @Override
    public String toString() {
        return version;
    }
}
