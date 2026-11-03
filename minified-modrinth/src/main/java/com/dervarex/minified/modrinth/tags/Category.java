package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.utils.json.JsonObject;

/**
 * A category projects can be in, like {@code optimization}
 */
public final class Category {
    private final String name;
    private final String icon;
    private final ProjectType projectType;
    private final String header;

    Category(JsonObject json) {
        name = ModrinthJson.string(json, "name");
        icon = ModrinthJson.string(json, "icon");
        projectType = ProjectType.fromApiValue(ModrinthJson.string(json, "project_type"));
        header = ModrinthJson.string(json, "header");
    }

    /** {@return the name, which is also what {@link com.dervarex.minified.modrinth.projects.Project#getCategories()} contains} */
    public String getName() {
        return name;
    }

    /** {@return the icon as SVG, empty if it has none} */
    public String getIcon() {
        return icon;
    }

    /** {@return the type of projects the category is for} */
    public ProjectType getProjectType() {
        return projectType;
    }

    /** {@return the group the category is shown in, like {@code features} or {@code resolutions}} */
    public String getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return name;
    }
}
