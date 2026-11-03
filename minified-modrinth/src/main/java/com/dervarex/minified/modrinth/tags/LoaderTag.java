package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.utils.json.JsonObject;

import java.util.List;

/**
 * A loader as Modrinth lists it
 */
public final class LoaderTag {
    private final String name;
    private final String icon;
    private final List<ProjectType> supportedProjectTypes;

    LoaderTag(JsonObject json) {
        name = ModrinthJson.string(json, "name");
        icon = ModrinthJson.string(json, "icon");
        supportedProjectTypes = ModrinthJson.list(json, "supported_project_types",
                value -> ProjectType.fromApiValue(value.asString()));
    }

    /** {@return the API value of the loader, like {@code fabric}} */
    public String getName() {
        return name;
    }

    /** {@return the icon as SVG} */
    public String getIcon() {
        return icon;
    }

    /** {@return the types of projects that can be made for the loader} */
    public List<ProjectType> getSupportedProjectTypes() {
        return supportedProjectTypes;
    }

    /**
     * Describes the loader, for logging
     *
     * @return the name
     */
    @Override
    public String toString() {
        return name;
    }
}
