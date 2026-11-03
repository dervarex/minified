package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.projects.ProjectType;

/**
 * Generic public tag model used by the Modrinth tag endpoints.
 */
public class Tag {
    /** The ID of the tag */
    public String id;
    /** The display name of the tag */
    public String name;
    /** A description of the tag */
    public String description;
    /** The icon of the tag */
    public String icon;
    /** The header of the tag */
    public String header;
    /** The value the API uses for the tag */
    public String value;
    /** Whether Modrinth features the tag */
    public boolean featured;
    /** The project type of the tag */
    public ProjectType projectType;
    /** The project types the tag can be used for */
    public String[] projectTypes;

    public Tag() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getHeader() {
        return header;
    }

    public String getValue() {
        return value;
    }

    public boolean isFeatured() {
        return featured;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public String[] getProjectTypes() {
        return projectTypes;
    }
}

