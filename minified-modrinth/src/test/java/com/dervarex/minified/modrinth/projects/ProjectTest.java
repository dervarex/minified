package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.exceptions.ModrinthStateException;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void hasTag_returnsTrue_whenTagExists() {
        Project project = new Project();
        project.categories = new String[]{"a", "b"};
        assertTrue(project.hasTag("a"));
    }

    @Test
    void hasTag_returnsFalse_whenTagMissing() {
        Project project = new Project();
        project.categories = new String[]{"a", "b"};
        assertFalse(project.hasTag("c"));
    }

    @Test
    void hasTag_returnsFalse_whenArrayNull() {
        Project project = new Project();
        assertFalse(project.hasTag("a"));
    }

    @Test
    void hasDisplayTag_returnsTrue_whenTagExists() {
        Project project = new Project();
        project.displayCategories = new String[]{"x"};
        assertTrue(project.hasDisplayTag("x"));
    }

    @Test
    void hasAdditionalTag_returnsTrue_whenTagExists() {
        Project project = new Project();
        project.additionalCategories = new String[]{"y"};
        assertTrue(project.hasAdditionalTag("y"));
    }

    @Test
    void hasLoader_returnsTrue_whenLoaderStringExists() {
        Project project = new Project();
        project.loaders = new String[]{"fabric"};
        assertTrue(project.hasLoader("fabric"));
    }

    @Test
    void hasLoader_returnsFalse_whenLoaderNull() {
        Project project = new Project();
        project.loaders = new String[]{"fabric"};
        assertFalse(project.hasLoader((ModLoader) null));
    }

    @Test
    void supportsVersion_returnsTrue_whenVersionExists() {
        Project project = new Project();
        project.gameVersions = new String[]{"1.20"};
        assertTrue(project.supportsVersion("1.20"));
    }

    @Test
    void hasLoader_returnsTrue_whenModLoaderExists() {
        Project project = new Project();
        project.loaders = new String[]{"fabric"};
        assertTrue(project.hasLoader(ModLoader.FABRIC));
    }

    @Test
    void supportsVersion_returnsFalse_whenVersionMissing() {
        Project project = new Project();
        project.gameVersions = new String[]{"1.20"};
        assertFalse(project.supportsVersion("1.19"));
    }

    @Test
    void getDisplayTags_returnsDisplayCategories() {
        Project project = new Project();
        project.displayCategories = new String[]{"x"};
        assertArrayEquals(new String[]{"x"}, project.getDisplayTags());
    }

    @Test
    void getAdditionalTags_returnsAdditionalCategories() {
        Project project = new Project();
        project.additionalCategories = new String[]{"y"};
        assertArrayEquals(new String[]{"y"}, project.getAdditionalTags());
    }

    @Test
    void supportsEnvironment_returnsTrue_whenEnvironmentExists() {
        Project project = new Project();
        project.environment = new Environment[]{Environment.CLIENT_AND_SERVER};
        assertTrue(project.supportsEnvironment(Environment.CLIENT_AND_SERVER));
    }

    @Test
    void hasDisclosure_returnsTrue_whenDisclosureExists() {
        Project project = new Project();
        project.disclosureTypes = new DisclosureType[]{DisclosureType.AI_CONTENT};
        assertTrue(project.hasDisclosure(DisclosureType.AI_CONTENT));
    }

    @Test
    void getFeaturedGallery_returnsFeaturedImage() {
        Project project = new Project();
        GalleryImage first = new GalleryImage();
        first.featured = false;
        GalleryImage second = new GalleryImage();
        second.featured = true;
        project.gallery = new GalleryImage[]{first, second};
        assertEquals(second, project.getFeaturedGallery());
    }

    @Test
    void getFeaturedGallery_returnsNull_whenNoFeatured() {
        Project project = new Project();
        GalleryImage image = new GalleryImage();
        image.featured = false;
        project.gallery = new GalleryImage[]{image};
        assertNull(project.getFeaturedGallery());
    }

    @Test
    void getFeaturedGallery_returnsNull_whenGalleryNull() {
        Project project = new Project();
        assertNull(project.getFeaturedGallery());
    }

    @Test
    void getLatestVersion_throwsModrinthStateException_whenNoClient() {
        Project project = new Project();
        project.id = "test";
        assertThrows(ModrinthStateException.class, () -> project.getLatestVersion("1.20", ModLoader.FABRIC));
    }

    @Test
    void getLatestVersionWithOptions_throwsModrinthStateException_whenNoClient() {
        Project project = new Project();
        project.id = "test";
        assertThrows(ModrinthStateException.class, () -> project.getLatestVersion((com.dervarex.minified.modrinth.VersionSearchOptions) null));
    }

    @Test
    void getTags_returnsCategories() {
        Project project = new Project();
        project.categories = new String[]{"a"};
        assertArrayEquals(new String[]{"a"}, project.getTags());
    }

    @Test
    void setTags_setsCategories() {
        Project project = new Project();
        project.setTags(new String[]{"b"});
        assertArrayEquals(new String[]{"b"}, project.categories);
    }
}