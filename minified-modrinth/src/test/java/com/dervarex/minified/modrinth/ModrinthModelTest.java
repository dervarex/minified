package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.projects.DisclosureType;
import com.dervarex.minified.modrinth.projects.Environment;
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
                .environment(Environment.CLIENT_ONLY)
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
        assertArrayEquals(new String[]{"client_only"}, request.environment);
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

    @Test
    void projectEnvironmentAndDisclosureConvenienceMethodsWork() {
        Project project = new Project();
        project.environment = new Environment[]{Environment.CLIENT_ONLY};
        project.disclosureTypes = new DisclosureType[]{DisclosureType.AI_CONTENT_CODE};

        assertTrue(project.supportsEnvironment(Environment.CLIENT_ONLY));
        assertFalse(project.supportsEnvironment(Environment.SERVER_ONLY));
        assertTrue(project.hasDisclosure(DisclosureType.AI_CONTENT_CODE));
        assertFalse(project.hasDisclosure(DisclosureType.ADVERTISEMENTS));
    }

    @Test
    void environmentFallsBackToUnknownForUnrecognizedValues() {
        assertEquals(Environment.CLIENT_ONLY, Environment.fromApiValue("client_only"));
        assertEquals(Environment.UNKNOWN, Environment.fromApiValue("something_new"));
        assertEquals(DisclosureType.TELEMETRY_OPT_IN, DisclosureType.fromApiValue("telemetry_opt_in"));
        assertEquals(DisclosureType.UNKNOWN, DisclosureType.fromApiValue("something_new"));
    }
}

