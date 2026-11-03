package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;

/**
 * A donation platform projects can link to
 */
public final class DonationPlatform {
    private final String id;
    private final String name;

    DonationPlatform(JsonObject json) {
        id = ModrinthJson.string(json, "short");
        name = ModrinthJson.string(json, "name");
    }

    /** {@return the ID, like {@code patreon}} */
    public String getId() {
        return id;
    }

    /** {@return the display name, like {@code Patreon}} */
    public String getName() {
        return name;
    }

    /**
     * Describes the platform, for logging
     *
     * @return the ID
     */
    @Override
    public String toString() {
        return id;
    }
}
