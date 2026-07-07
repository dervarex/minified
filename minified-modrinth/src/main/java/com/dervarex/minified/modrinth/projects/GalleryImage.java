package com.dervarex.minified.modrinth.projects;

import java.time.Instant;

public class GalleryImage {

    public String url;
    public boolean featured;
    public String title;
    public String description;
    public Instant created;
    public int ordering;

    public GalleryImage() {
    }

    public String getUrl() {
        return url;
    }

    public boolean isFeatured() {
        return featured;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreated() {
        return created;
    }

    public int getOrdering() {
        return ordering;
    }
}