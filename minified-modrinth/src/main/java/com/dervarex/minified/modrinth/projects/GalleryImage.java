package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;

import java.time.Instant;

/**
 * An image from the gallery of a project
 */
public final class GalleryImage {
    private final String url;
    private final String rawUrl;
    private final boolean featured;
    private final String title;
    private final String description;
    private final Instant created;
    private final int ordering;

    GalleryImage(JsonObject json) {
        url = ModrinthJson.string(json, "url");
        rawUrl = ModrinthJson.string(json, "raw_url");
        featured = ModrinthJson.bool(json, "featured");
        title = ModrinthJson.string(json, "title");
        description = ModrinthJson.string(json, "description");
        created = ModrinthJson.instant(json, "created");
        ordering = (int) ModrinthJson.number(json, "ordering");
    }

    /** {@return the URL of the image} */
    public String getUrl() {
        return url;
    }

    /** {@return the URL of the image before Modrinth optimized it} */
    public String getRawUrl() {
        return rawUrl;
    }

    /** {@return whether the image is the featured one of the project} */
    public boolean isFeatured() {
        return featured;
    }

    /** {@return the title of the image, {@code null} if it has none} */
    public String getTitle() {
        return title;
    }

    /** {@return the description of the image, {@code null} if it has none} */
    public String getDescription() {
        return description;
    }

    /** {@return when the image was added} */
    public Instant getCreated() {
        return created;
    }

    /** {@return where the image goes in the gallery, lower ones come first} */
    public int getOrdering() {
        return ordering;
    }
}
