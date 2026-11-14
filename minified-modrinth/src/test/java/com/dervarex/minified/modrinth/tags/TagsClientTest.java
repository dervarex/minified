package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagsClientTest {

    private final TagsClient tags = Modrinth.connect().tags();

    @Test
    void parsesCategories() {
        Category category = new Category(JsonParser.parse(
                "{\"icon\":\"<svg/>\",\"name\":\"optimization\",\"project_type\":\"mod\",\"header\":\"categories\"}").asObject());
        assertEquals("optimization", category.getName());
        assertEquals("<svg/>", category.getIcon());
        assertEquals(ProjectType.MOD, category.getProjectType());
        assertEquals("categories", category.getHeader());
    }

    @Test
    void parsesLoaders() {
        LoaderTag loader = new LoaderTag(JsonParser.parse(
                "{\"icon\":\"<svg/>\",\"name\":\"fabric\",\"supported_project_types\":[\"mod\",\"modpack\"]}").asObject());
        assertEquals("fabric", loader.getName());
        assertEquals(List.of(ProjectType.MOD, ProjectType.MODPACK), loader.getSupportedProjectTypes());
    }

    @Test
    void parsesGameVersions() {
        GameVersion version = new GameVersion(JsonParser.parse(
                "{\"version\":\"1.21\",\"version_type\":\"release\",\"date\":\"2024-06-13T00:00:00Z\",\"major\":true}").asObject());
        assertEquals("1.21", version.getVersion());
        assertEquals("release", version.getVersionType());
        assertEquals(Instant.parse("2024-06-13T00:00:00Z"), version.getDate());
        assertTrue(version.isMajor());
    }

    @Test
    void parsesDonationPlatforms() {
        DonationPlatform platform = new DonationPlatform(JsonParser.parse("{\"short\":\"bmac\",\"name\":\"Buy Me A Coffee\"}").asObject());
        assertEquals("bmac", platform.getId());
        assertEquals("Buy Me A Coffee", platform.getName());
    }

    @Test
    @Tag("manual")
    void everyTagRouteWorks() {
        assertTrue(tags.categories().stream().anyMatch(category -> category.getName().equals("optimization")));
        assertTrue(tags.loaders().stream().anyMatch(loader -> loader.getName().equals("fabric")));
        assertTrue(tags.gameVersions().stream().anyMatch(version -> version.getVersion().equals("1.21.11")));
        assertTrue(tags.donationPlatforms().stream().anyMatch(platform -> platform.getId().equals("patreon")));
        assertTrue(tags.projectTypes().contains(ProjectType.MOD));
    }
}
