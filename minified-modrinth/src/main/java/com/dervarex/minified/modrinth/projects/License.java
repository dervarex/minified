package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;

/**
 * The license of a project
 */
public final class License {
    private final String id;
    private final String name;
    private final String url;

    License(JsonObject json) {
        id = ModrinthJson.string(json, "id");
        name = ModrinthJson.string(json, "name");
        url = ModrinthJson.string(json, "url");
    }

    /** {@return the identifier license, like {@code MIT}, or {@code LicenseRef-xyz} for custom ones} */
    public String getId() {
        return id;
    }

    /** {@return the name of the license, can be empty (usually if it's a custom license)} */
    public String getName() {
        return name;
    }

    /** {@return a link to the license text, {@code null} if the project doesn't link one} */
    public String getUrl() {
        return url;
    }

    /**
     * Describes the license, for logging
     *
     * @return the license ID
     */
    @Override
    public String toString() {
        return id;
    }
}
