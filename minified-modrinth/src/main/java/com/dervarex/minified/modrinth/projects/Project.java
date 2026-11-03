package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.exceptions.ModrinthStateException;
import com.dervarex.minified.modrinth.VersionSearchOptions;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.versions.Version;

import java.time.Instant;

/**
 * Represents a Modrinth project.
 */
public class Project {

    // IDs

    /** The project ID, only set by the detail endpoint ({@link ProjectsClient#get(String)}). */
    public String id;

    /** The project ID, only set by the search endpoint ({@link ProjectsClient#search(com.dervarex.minified.modrinth.SearchRequest)}). */
    public String projectId;

    /** The readable identifier of the project, like {@code sodium} */
    public String slug;

    // Basic information

    /** The display name of the project */
    public String title;
    /** Short description of the project */
    public String description;

    /** The full project description, only set by the detail endpoint. */
    public String body;

    /** Legacy field, always {@code null} (according to the Modrinth API) */
    public String bodyUrl;

    /** The type of content the project provides */
    public ProjectType projectType;
    /** The monetization status of the project */
    public String monetizationStatus;

    // Ownership

    /** The display name of the project author */
    public String author;
    /** The ID of the project author */
    public String authorId;

    /** The organization that owns the project, if any */
    public String organization;
    /** The ID of the organization that owns the project, if any */
    public String organizationId;

    /** The ID of the team behind the project, see {@link com.dervarex.minified.modrinth.teams.TeamsClient} */
    public String team;

    // Compatibility

    /** Whether the project is needed on the client. Prefer {@link #environment} */
    public SideSupport clientSide;
    /** Whether the project is needed on the server. Prefer {@link #environment} */
    public SideSupport serverSide;

    /**
     * Where the project runs
     */
    public Environment[] environment;

    // Tags

    /** The categories assigned to the project */
    public String[] categories;
    /** The categories shown on the project's search card */
    public String[] displayCategories;
    /** Categories assigned to the project that aren't primary categories */
    public String[] additionalCategories;
    /** The IDs of every version published under the project */
    public String[] versions;
    /** The Minecraft versions the project supports */
    public String[] gameVersions;
    /** The mod loaders and/or plugin platforms the project supports */
    public String[] loaders;

    /**
     * Content disclosures (AI content, telemetry, advertisements and so on) the project is tagged with
     */
    public DisclosureType[] disclosureTypes;

    // Statistics

    /** The number of followers */
    public long follows;

    /** The number of followers */
    public long followers;

    /** The total number of downloads */
    public long downloads;

    // Appearance

    /** The URL of the project's icon */
    public String iconUrl;

    /** The URL of the raw, unoptimized icon */
    public String rawIconUrl;

    /** The project's display color */
    public Integer color;

    // Links

    /** The ID of the moderation thread of the project */
    public String threadId;

    /** The URL of the project's issue tracker, if any */
    public String issuesUrl;
    /** The URL of the project's source code repository, if any */
    public String sourceUrl;
    /** The URL of the project's wiki, if any */
    public String wikiUrl;
    /** The invite URL of the project's Discord server, if any */
    public String discordUrl;

    /** The donation links listed for the project */
    public DonationUrl[] donationUrls;

    // Metadata

    /** The license of the project */
    public ProjectLicense license;

    /** The images in the project's gallery */
    public GalleryImage[] gallery;

    /** When the project was published */
    public Instant published;
    /** When the project was last updated */
    public Instant updated;

    transient Modrinth client;

    public Project() {
    }

    // Convenience methods

    /** Returns the project's categories, the same as {@link #categories} */
    public String[] getTags() {
        return categories;
    }

    /** Sets the project's categories */
    public void setTags(String[] tags) {
        this.categories = tags;
    }

    /** Returns the categories shown on the project's search card */
    public String[] getDisplayTags() {
        return displayCategories;
    }

    /** Returns the categories assigned to the project that aren't primary categories */
    public String[] getAdditionalTags() {
        return additionalCategories;
    }

    /** Checks if the project has a category, like {@code optimization}. */
    public boolean hasTag(String tag) {
        return contains(categories, tag);
    }

    /** Checks whether a category is shown on the project's search card. */
    public boolean hasDisplayTag(String tag) {
        return contains(displayCategories, tag);
    }

    /** Checks if the project has an additional category. */
    public boolean hasAdditionalTag(String tag) {
        return contains(additionalCategories, tag);
    }

    /** Checks whether the project supports a loader, by its API value like {@code fabric}. */
    public boolean hasLoader(String loader) {
        return contains(loaders, loader);
    }

    /** Checks if the project supports a mod loader. */
    public boolean hasLoader(ModLoader loader) {
        return loader != null && hasLoader(loader.getApiValue());
    }

    /** Checks whether the project supports a Minecraft version, like {@code 1.21.11}. */
    public boolean supportsVersion(String version) {
        return contains(gameVersions, version);
    }

    /** Checks if the project has an {@link Environment} value, like {@link Environment#CLIENT_ONLY}. */
    public boolean supportsEnvironment(Environment value) {
        return contains(environment, value);
    }

    /**
     * Checks whether the project is tagged with a disclosure
     */
    public boolean hasDisclosure(DisclosureType value) {
        return contains(disclosureTypes, value);
    }

    /** Returns the gallery image marked as featured, or {@code null} if the project has none. */
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

    /** Returns the license of the project. */
    public com.dervarex.minified.modrinth.projects.ProjectLicense getLicense() {
        return license;
    }

    /** Sets the license of the project. */
    public void setLicense(com.dervarex.minified.modrinth.projects.ProjectLicense license) {
        this.license = license;
    }

    /**
     * Resolves the latest version of the project for a game version and loaders
     */
    public Version getLatestVersion(String gameVersion, ModLoader... loaders) {
        return getLatestVersion(new String[]{gameVersion}, loaders == null ? null : toStrings(loaders));
    }

    /**
     * Resolves the latest version of the project for any of the game versions and loaders, given by their API values
     */
    public Version getLatestVersion(String[] gameVersions, String... loaders) {
        ensureClient();
        VersionSearchOptions.Builder builder = VersionSearchOptions.builder();
        builder.gameVersions(gameVersions);
        builder.loaders(loaders);
        return client.versions().resolveLatest(id != null ? id : projectId, builder.build());
    }

    /**
     * Resolves the latest version of the project matching the options
     */
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

    private static <T> boolean contains(T[] array, T value) {
        if (array == null || value == null) {
            return false;
        }

        for (T entry : array) {
            if (value.equals(entry)) {
                return true;
            }
        }

        return false;
    }
}