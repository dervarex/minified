package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.loaders.Loader;
import com.dervarex.minified.utils.json.JsonObject;

import java.time.Instant;
import java.util.List;

/**
 * A Modrinth project, e.g. a mod, resource pack or shader. Get one from {@link ProjectsClient}.
 */
public final class Project {
    private final String id;
    private final String slug;
    private final String title;
    private final String description;
    private final String body;
    private final ProjectType projectType;
    private final String monetizationStatus;
    private final String team;
    private final String organization;
    private final List<Environment> environment;
    private final List<String> categories;
    private final List<String> additionalCategories;
    private final List<String> versions;
    private final List<String> gameVersions;
    private final List<String> loaders;
    private final long followers;
    private final long downloads;
    private final String iconUrl;
    private final String rawIconUrl;
    private final Integer color;
    private final String issuesUrl;
    private final String sourceUrl;
    private final String wikiUrl;
    private final String discordUrl;
    private final List<DonationUrl> donationUrls;
    private final License license;
    private final List<GalleryImage> gallery;
    private final Instant published;
    private final Instant updated;

    Project(JsonObject json) {
        id = ModrinthJson.string(json, "id");
        slug = ModrinthJson.string(json, "slug");
        title = ModrinthJson.string(json, "title");
        description = ModrinthJson.string(json, "description");
        body = ModrinthJson.string(json, "body");
        projectType = ProjectType.fromApiValue(ModrinthJson.string(json, "project_type"));
        monetizationStatus = ModrinthJson.string(json, "monetization_status");
        team = ModrinthJson.string(json, "team");
        organization = ModrinthJson.string(json, "organization");
        environment = ModrinthJson.list(json, "environment", value -> Environment.fromApiValue(value.asString()));
        categories = ModrinthJson.strings(json, "categories");
        additionalCategories = ModrinthJson.strings(json, "additional_categories");
        versions = ModrinthJson.strings(json, "versions");
        gameVersions = ModrinthJson.strings(json, "game_versions");
        loaders = ModrinthJson.strings(json, "loaders");
        followers = ModrinthJson.number(json, "followers");
        downloads = ModrinthJson.number(json, "downloads");
        iconUrl = ModrinthJson.string(json, "icon_url");
        rawIconUrl = ModrinthJson.string(json, "raw_icon_url");
        color = ModrinthJson.integer(json, "color");
        issuesUrl = ModrinthJson.string(json, "issues_url");
        sourceUrl = ModrinthJson.string(json, "source_url");
        wikiUrl = ModrinthJson.string(json, "wiki_url");
        discordUrl = ModrinthJson.string(json, "discord_url");
        donationUrls = ModrinthJson.objects(json, "donation_urls", DonationUrl::new);
        JsonObject licenseJson = ModrinthJson.object(json, "license");
        license = licenseJson == null ? null : new License(licenseJson);
        gallery = ModrinthJson.objects(json, "gallery", GalleryImage::new);
        published = ModrinthJson.instant(json, "published");
        updated = ModrinthJson.instant(json, "updated");
    }

    /** {@return the ID of the project, like {@code AANobbMI}} */
    public String getId() {
        return id;
    }

    /** {@return the readable identifier of the project, like {@code sodium}} */
    public String getSlug() {
        return slug;
    }

    /** {@return the display name of the project} */
    public String getTitle() {
        return title;
    }

    /** {@return the short description of the project} */
    public String getDescription() {
        return description;
    }

    /** {@return the full description of the project, in Markdown} */
    public String getBody() {
        return body;
    }

    /** {@return what the project is, like a mod or a shader} */
    public ProjectType getProjectType() {
        return projectType;
    }

    /** {@return the monetization status of the project, like {@code monetized}} */
    public String getMonetizationStatus() {
        return monetizationStatus;
    }

    /** {@return the ID of the team behind the project, for {@link com.dervarex.minified.modrinth.teams.TeamsClient#members(String)}} */
    public String getTeam() {
        return team;
    }

    /** {@return the ID of the organization that owns the project, {@code null} if it has none} */
    public String getOrganization() {
        return organization;
    }

    /** {@return where the project runs} */
    public List<Environment> getEnvironment() {
        return environment;
    }

    /** {@return the primary categories of the project, like {@code optimization}} */
    public List<String> getCategories() {
        return categories;
    }

    /** {@return the categories of the project that aren't its primary ones} */
    public List<String> getAdditionalCategories() {
        return additionalCategories;
    }

    /** {@return the IDs of every version of the project} */
    public List<String> getVersions() {
        return versions;
    }

    /** {@return the Minecraft versions the project supports} */
    public List<String> getGameVersions() {
        return gameVersions;
    }

    /** {@return the API values of the loaders the project supports, like {@code fabric}} */
    public List<String> getLoaders() {
        return loaders;
    }

    /** {@return the number of followers} */
    public long getFollowers() {
        return followers;
    }

    /** {@return the total number of downloads} */
    public long getDownloads() {
        return downloads;
    }

    /** {@return the URL of the project's icon, {@code null} if it has none} */
    public String getIconUrl() {
        return iconUrl;
    }

    /** {@return the URL of the project's icon before Modrinth optimized it, {@code null} if it has none} */
    public String getRawIconUrl() {
        return rawIconUrl;
    }

    /** {@return the display color of the project as RGB, {@code null} if it has none} */
    public Integer getColor() {
        return color;
    }

    /** {@return the URL of the project's issue tracker, {@code null} if it has none} */
    public String getIssuesUrl() {
        return issuesUrl;
    }

    /** {@return the URL of the project's source code, {@code null} if it has none} */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /** {@return the URL of the project's wiki, {@code null} if it has none} */
    public String getWikiUrl() {
        return wikiUrl;
    }

    /** {@return the invite URL of the project's Discord server, {@code null} if it has none} */
    public String getDiscordUrl() {
        return discordUrl;
    }

    /** {@return the donation links of the project} */
    public List<DonationUrl> getDonationUrls() {
        return donationUrls;
    }

    /** {@return the license of the project} */
    public License getLicense() {
        return license;
    }

    /** {@return the images in the project's gallery} */
    public List<GalleryImage> getGallery() {
        return gallery;
    }

    /** {@return when the project was published} */
    public Instant getPublished() {
        return published;
    }

    /** {@return when the project was last updated} */
    public Instant getUpdated() {
        return updated;
    }

    /**
     * Checks whether the project has a category, primary or additional
     *
     * @param category the category name, like {@code optimization}
     * @return whether the project has it
     */
    public boolean hasCategory(String category) {
        return categories.contains(category) || additionalCategories.contains(category);
    }

    /**
     * Checks whether the project supports a loader
     *
     * @param loader the loader
     * @return whether the project supports it, {@code false} for {@code null}
     */
    public boolean hasLoader(Loader loader) {
        return loader != null && loaders.contains(loader.getApiValue());
    }

    /**
     * Checks whether the project supports a Minecraft version
     *
     * @param gameVersion the Minecraft version, like {@code 1.21.11}
     * @return whether the project supports it
     */
    public boolean supportsGameVersion(String gameVersion) {
        return gameVersions.contains(gameVersion);
    }

    /** {@return the gallery image marked as featured, {@code null} if there is none} */
    public GalleryImage getFeaturedImage() {
        return gallery.stream().filter(GalleryImage::isFeatured).findFirst().orElse(null);
    }

    /**
     * Describes the project, for logging purposes
     *
     * @return the slug and ID
     */
    @Override
    public String toString() {
        return "Project{" + slug + ", " + id + "}";
    }
}
