package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.loaders.ShaderLoader;
import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    // source: https://api.modrinth.com/v2/project/sodium, trimmed to what we we need
    private static final String SODIUM = """
            {"id":"AANobbMI","slug":"sodium","title":"Sodium","description":"The fastest rendering mod",
             "body":"# Sodium","project_type":"mod","monetization_status":"monetized","team":"4reLOAKe",
             "organization":"LjcZDkRW","environment":["client_only"],"categories":["optimization"],
             "additional_categories":["utility"],"versions":["yaoBL9D9","YAGZ1cCS"],"game_versions":["1.21.11"],
             "loaders":["fabric","neoforge","quilt"],"followers":20000,"downloads":100000000,
             "icon_url":"https://cdn/icon.png","raw_icon_url":"https://cdn/icon-raw.png","color":7,
             "issues_url":"https://issues","source_url":"https://source","wiki_url":null,"discord_url":"https://discord",
             "donation_urls":[{"id":"ko-fi","platform":"Ko-fi","url":"https://caffeinemc.net/donate"}],
             "license":{"id":"LicenseRef-Polyform-Shield-1.0.0","name":"","url":"https://license"},
             "gallery":[{"url":"https://cdn/1.webp","raw_url":"https://cdn/1-raw.webp","featured":false,"title":"One",
                         "description":null,"created":"2023-06-25T21:35:39.891789Z","ordering":0},
                        {"url":"https://cdn/2.webp","raw_url":"https://cdn/2-raw.webp","featured":true,"title":"Two",
                         "description":"Second","created":"2023-06-25T21:35:39.891789Z","ordering":1}],
             "published":"2021-01-03T00:53:34.185936Z","updated":"2026-10-01T00:00:00Z"}""";

    private static Project project(String json) {
        return new Project(JsonParser.parse(json).asObject());
    }

    @Test
    void readsEveryField() {
        Project project = project(SODIUM);

        assertEquals("AANobbMI", project.getId());
        assertEquals("sodium", project.getSlug());
        assertEquals("Sodium", project.getTitle());
        assertEquals("The fastest rendering mod", project.getDescription());
        assertEquals("# Sodium", project.getBody());
        assertEquals(ProjectType.MOD, project.getProjectType());
        assertEquals("monetized", project.getMonetizationStatus());
        assertEquals("4reLOAKe", project.getTeam());
        assertEquals("LjcZDkRW", project.getOrganization());
        assertEquals(List.of(Environment.CLIENT_ONLY), project.getEnvironment());
        assertEquals(List.of("optimization"), project.getCategories());
        assertEquals(List.of("utility"), project.getAdditionalCategories());
        assertEquals(List.of("yaoBL9D9", "YAGZ1cCS"), project.getVersions());
        assertEquals(List.of("1.21.11"), project.getGameVersions());
        assertEquals(List.of("fabric", "neoforge", "quilt"), project.getLoaders());
        assertEquals(20000, project.getFollowers());
        assertEquals(100000000, project.getDownloads());
        assertEquals("https://cdn/icon.png", project.getIconUrl());
        assertEquals("https://cdn/icon-raw.png", project.getRawIconUrl());
        assertEquals(7, project.getColor());
        assertEquals("https://issues", project.getIssuesUrl());
        assertEquals("https://source", project.getSourceUrl());
        assertNull(project.getWikiUrl());
        assertEquals("https://discord", project.getDiscordUrl());
        assertEquals("ko-fi", project.getDonationUrls().getFirst().getId());
        assertEquals("Ko-fi", project.getDonationUrls().getFirst().getPlatform());
        assertEquals("https://caffeinemc.net/donate", project.getDonationUrls().getFirst().getUrl());
        assertEquals("LicenseRef-Polyform-Shield-1.0.0", project.getLicense().getId());
        assertEquals("", project.getLicense().getName());
        assertEquals("https://license", project.getLicense().getUrl());
        assertEquals(2, project.getGallery().size());
        assertEquals(Instant.parse("2021-01-03T00:53:34.185936Z"), project.getPublished());
        assertEquals(Instant.parse("2026-10-01T00:00:00Z"), project.getUpdated());
    }

    @Test
    void readsGalleryImages() {
        GalleryImage image = project(SODIUM).getGallery().get(1);

        assertEquals("https://cdn/2.webp", image.getUrl());
        assertEquals("https://cdn/2-raw.webp", image.getRawUrl());
        assertTrue(image.isFeatured());
        assertEquals("Two", image.getTitle());
        assertEquals("Second", image.getDescription());
        assertEquals(Instant.parse("2023-06-25T21:35:39.891789Z"), image.getCreated());
        assertEquals(1, image.getOrdering());
    }

    @Test
    void missingListsAreEmpty() {
        Project project = project("{\"id\":\"x\"}");

        assertTrue(project.getCategories().isEmpty());
        assertTrue(project.getLoaders().isEmpty());
        assertTrue(project.getGallery().isEmpty());
        assertTrue(project.getDonationUrls().isEmpty());
        assertTrue(project.getEnvironment().isEmpty());
        assertNull(project.getLicense());
        assertNull(project.getFeaturedImage());
    }

    @Test
    void listsCantBeChanged() {
        assertThrows(UnsupportedOperationException.class, () -> project(SODIUM).getCategories().add("x"));
    }

    @Test
    void hasCategory_checksPrimaryAndAdditional() {
        Project project = project(SODIUM);
        assertTrue(project.hasCategory("optimization"));
        assertTrue(project.hasCategory("utility"));
        assertFalse(project.hasCategory("magic"));
    }

    @Test
    void hasLoader() {
        Project project = project(SODIUM);
        assertTrue(project.hasLoader(ModLoader.FABRIC));
        assertFalse(project.hasLoader(ModLoader.FORGE));
        assertFalse(project.hasLoader(ShaderLoader.IRIS));
        assertFalse(project.hasLoader(null));
    }

    @Test
    void supportsGameVersion() {
        Project project = project(SODIUM);
        assertTrue(project.supportsGameVersion("1.21.11"));
        assertFalse(project.supportsGameVersion("1.8.9"));
    }

    @Test
    void getFeaturedImage_returnsTheFeaturedOne() {
        assertEquals("Two", project(SODIUM).getFeaturedImage().getTitle());
    }

    @Test
    void unknownValues_becomeUnknown() {
        Project project = project("{\"project_type\":\"something_new\",\"environment\":[\"somewhere\"]}");
        assertEquals(ProjectType.UNKNOWN, project.getProjectType());
        assertEquals(List.of(Environment.UNKNOWN), project.getEnvironment());
    }
}
