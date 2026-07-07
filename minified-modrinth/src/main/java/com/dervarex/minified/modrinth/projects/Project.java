package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.ModrinthStateException;
import com.dervarex.minified.modrinth.VersionSearchOptions;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.versions.Version;

import java.time.Instant;

/**
 * Represents a Modrinth project.
 */
public class Project {

    // ------------------------------------------------------------------------
    // IDs
    // ------------------------------------------------------------------------

    /** Project ID returned by the detail endpoint. */
    public String id;

    /** Project ID returned by the search endpoint. */
    public String projectId;

    public String slug;

    // ------------------------------------------------------------------------
    // Basic information
    // ------------------------------------------------------------------------

    public String title;
    public String description;

    /** Detail endpoint only. */
    public String body;

    /** Legacy field, always null according to the API. */
    public String bodyUrl;

    public ProjectType projectType;
    public String monetizationStatus;

    // ------------------------------------------------------------------------
    // Ownership
    // ------------------------------------------------------------------------

    public String author;
    public String authorId;

    public String organization;
    public String organizationId;

    public String team;

    // Compatibility

    public SideSupport clientSide;
    public SideSupport serverSide;

    // Tags

    public String[] categories;
    public String[] displayCategories;
    public String[] additionalCategories;
    public String[] versions;
    public String[] gameVersions;
    public String[] loaders;

    // Statistics

    /** Search endpoint. */
    public long follows;

    /** Detail endpoint. */
    public long followers;

    public long downloads;

    // Appearance

    public String iconUrl;
    public Integer color;

    // Links

    public String threadId;

    public String issuesUrl;
    public String sourceUrl;
    public String wikiUrl;
    public String discordUrl;

    public DonationUrl[] donationUrls;

    // Metadata

    public com.dervarex.minified.modrinth.projects.ProjectLicense license;

    public GalleryImage[] gallery;

    public Instant published;
    public Instant updated;

    transient Modrinth client;

    public Project() {
    }

    // Convenience methods

    public String[] getTags() {
        return categories;
    }

    public void setTags(String[] tags) {
        this.categories = tags;
    }

    public String[] getDisplayTags() {
        return displayCategories;
    }

    public String[] getAdditionalTags() {
        return additionalCategories;
    }

    public boolean hasTag(String tag) {
        return contains(categories, tag);
    }

    public boolean hasDisplayTag(String tag) {
        return contains(displayCategories, tag);
    }

    public boolean hasAdditionalTag(String tag) {
        return contains(additionalCategories, tag);
    }

    public boolean hasLoader(String loader) {
        return contains(loaders, loader);
    }

    public boolean hasLoader(ModLoader loader) {
        return loader != null && hasLoader(loader.getApiValue());
    }

    public boolean supportsVersion(String version) {
        return contains(gameVersions, version);
    }

    public GalleryImage getFeaturedGallery() {
        if (gallery == null) {
            return null;
        }

        for (GalleryImage image : gallery) {
            if (image != null && image.featured) {
                return image;
            }
        }

        return null;
    }

    public com.dervarex.minified.modrinth.projects.ProjectLicense getLicense() {
        return license;
    }

    public void setLicense(com.dervarex.minified.modrinth.projects.ProjectLicense license) {
        this.license = license;
    }

    public Version getLatestVersion(String gameVersion, ModLoader... loaders) {
        return getLatestVersion(new String[]{gameVersion}, loaders == null ? null : toStrings(loaders));
    }

    public Version getLatestVersion(String[] gameVersions, String... loaders) {
        ensureClient();
        VersionSearchOptions.Builder builder = VersionSearchOptions.builder();
        builder.gameVersions(gameVersions);
        builder.loaders(loaders);
        return client.versions().resolveLatest(id != null ? id : projectId, builder.build());
    }

    public Version getLatestVersion(VersionSearchOptions options) {
        ensureClient();
        return client.versions().resolveLatest(id != null ? id : projectId, options);
    }

    private static String[] toStrings(ModLoader[] loaders) {
        String[] values = new String[loaders.length];
        int count = 0;
        for (ModLoader loader : loaders) {
            if (loader != null) {
                values[count++] = loader.toString();
            }
        }
        String[] result = new String[count];
        System.arraycopy(values, 0, result, 0, count);
        return result;
    }

    private void ensureClient() {
        if (client == null) {
            throw new ModrinthStateException("This project is not attached to a Modrinth client");
        }
    }

    void attach(Modrinth client) {
        this.client = client;
    }

    private static boolean contains(String[] array, String value) {
        if (array == null || value == null) {
            return false;
        }

        for (String entry : array) {
            if (value.equals(entry)) {
                return true;
            }
        }

        return false;
    }
}