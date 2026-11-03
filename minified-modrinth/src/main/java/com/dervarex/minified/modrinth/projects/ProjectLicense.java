package com.dervarex.minified.modrinth.projects;

/**
 * The license of a project, e.g. {@code MIT}
 */
public class ProjectLicense {
    public String id;
    public String name;
    public String url;

    public ProjectLicense() {
    }

    public ProjectLicense(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}

