package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.projects.GalleryImage;
import com.dervarex.minified.modrinth.projects.Project;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.modrinth.projects.SideSupport;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ModrinthModelTest {
    @Test
    void searchRequestBuilderBuildsFacets() {
        SearchRequest request = SearchRequest.builder()
                .query("sodium")
                .projectType(ProjectType.MOD)
                .clientSide(SideSupport.REQUIRED)
                .serverSide(SideSupport.UNSUPPORTED)
                .categories("performance", "optimization")
                .gameVersions("1.21.8")
                .loaders(ModLoader.FABRIC)
                .index(SearchRequest.SearchIndex.DOWNLOADS)
                .limit(10)
                .offset(5)
                .build();

        assertEquals("sodium", request.query);
        assertEquals("downloads", request.index);
        assertEquals(10, request.limit);
        assertEquals(5, request.offset);
        assertNotNull(request.facets);
        assertFalse(request.facets.isEmpty());
    }

    @Test
    void projectConvenienceMethodsWork() {
        Project project = new Project();
        project.categories = new String[]{"performance", "optimization"};
        project.loaders = new String[]{"fabric", "quilt"};
        project.gameVersions = new String[]{"1.21.8", "1.21.7"};
        project.gallery = new GalleryImage[]{
                null,
                new GalleryImage()
        };
        project.gallery[1].featured = true;
        project.gallery[1].url = "https://example.invalid/gallery.png";
        project.gallery[1].created = Instant.parse("2024-01-01T00:00:00Z");

        assertTrue(project.hasTag("performance"));
        assertTrue(project.hasLoader(ModLoader.FABRIC));
        assertTrue(project.supportsVersion("1.21.8"));
        assertNotNull(project.getFeaturedGallery());
    }
}

