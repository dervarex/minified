package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;

/**
 * A donation link of a project, like a Patreon page
 */
public final class DonationUrl {
    private final String id;
    private final String platform;
    private final String url;

    DonationUrl(JsonObject json) {
        id = ModrinthJson.string(json, "id");
        platform = ModrinthJson.string(json, "platform");
        url = ModrinthJson.string(json, "url");
    }

    /** {@return the ID of the donation platform, like {@code ko-fi}} */
    public String getId() {
        return id;
    }

    /** {@return the display name of the donation platform, like {@code ko-fi}} */
    public String getPlatform() {
        return platform;
    }

    /** {@return the link} */
    public String getUrl() {
        return url;
    }
}
