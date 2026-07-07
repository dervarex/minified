package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.projects.ProjectType;

/**
 * Generic public tag model used by the Modrinth tag endpoints.
 */
public class Tag {
    public String id;
    public String name;
    public String description;
    public String icon;
    public String header;
    public String value;
    public boolean featured;
    public ProjectType projectType;
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

