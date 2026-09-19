package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.projects.Environment;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.modrinth.projects.SideSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchRequestTest {

    @Test
    void builder_setsQuery() {
        SearchRequest request = SearchRequest.builder().query("sodium").build();
        assertEquals("sodium", request.getQuery());
    }

    @Test
    void builder_setsDefaultIndex() {
        SearchRequest request = SearchRequest.builder().build();
        assertEquals("relevance", request.getIndex());
    }

    @Test
    void builder_indexString_setsIndex() {
        SearchRequest request = SearchRequest.builder().index("downloads").build();
        assertEquals("downloads", request.getIndex());
    }

    @Test
    void builder_indexEnum_setsIndex() {
        SearchRequest request = SearchRequest.builder()
                .index(SearchRequest.SearchIndex.DOWNLOADS)
                .build();
        assertEquals("downloads", request.getIndex());
    }

    @Test
    void builder_projectType_addsFacet() {
        SearchRequest request = SearchRequest.builder()
                .projectType(ProjectType.MOD)
                .build();
        assertEquals(ProjectType.MOD, request.projectType);
        assertEquals(List.of("project_type:mod"), request.facets.get(0));
    }

    @Test
    void builder_clientSide_addsFacet() {
        SearchRequest request = SearchRequest.builder()
                .clientSide(SideSupport.REQUIRED)
                .build();
        assertEquals(SideSupport.REQUIRED, request.clientSide);
        assertEquals(List.of("client_side:required"), request.facets.get(0));
    }

    @Test
    void builder_serverSide_addsFacet() {
        SearchRequest request = SearchRequest.builder()
                .serverSide(SideSupport.OPTIONAL)
                .build();
        assertEquals(SideSupport.OPTIONAL, request.serverSide);
        assertEquals(List.of("server_side:optional"), request.facets.get(0));
    }

    @Test
    void builder_environment_addsFacet() {
        SearchRequest request = SearchRequest.builder()
                .environment(Environment.CLIENT_ONLY)
                .build();
        assertArrayEquals(new String[]{"client_only"}, request.environment);
        assertEquals(List.of("environment:client_only"), request.facets.get(0));
    }

    @Test
    void builder_categories_trimsAndDistincts() {
        SearchRequest request = SearchRequest.builder()
                .categories(" combat ", "combat", "magic")
                .build();
        assertArrayEquals(new String[]{"combat", "magic"}, request.categories);
        assertEquals(List.of("categories:combat", "categories:magic"), request.facets.get(0));
    }

    @Test
    void builder_gameVersions_usesVersionsFacetKey() {
        SearchRequest request = SearchRequest.builder()
                .gameVersions("1.20")
                .build();
        assertEquals(List.of("versions:1.20"), request.facets.get(0));
    }

    @Test
    void builder_loadersModLoader_convertsToStrings() {
        SearchRequest request = SearchRequest.builder()
                .loaders(ModLoader.FABRIC)
                .build();
        assertArrayEquals(new String[]{"fabric"}, request.loaders);
        assertEquals(List.of("loaders:fabric"), request.facets.get(0));
    }

    @Test
    void builder_loadersString_trims() {
        SearchRequest request = SearchRequest.builder()
                .loaders(" fabric ", "forge")
                .build();
        assertArrayEquals(new String[]{"fabric", "forge"}, request.loaders);
    }

    @Test
    void builder_limit_keepsPositiveValue() {
        SearchRequest request = SearchRequest.builder().limit(20).build();
        assertEquals(20, request.limit);
    }

    @Test
    void builder_limit_usesZeroForNegative() {
        SearchRequest request = SearchRequest.builder().limit(-5).build();
        assertEquals(0, request.limit);
    }

    @Test
    void builder_offset_usesZeroForNegative() {
        SearchRequest request = SearchRequest.builder().offset(-1).build();
        assertEquals(0, request.offset);
    }

    @Test
    void builder_facet_addsGroup() {
        SearchRequest request = SearchRequest.builder()
                .facet("categories", "combat", "magic")
                .build();
        assertEquals(List.of("categories:combat", "categories:magic"), request.facets.get(0));
    }

    @Test
    void builder_facet_skipsEmptyValues() {
        SearchRequest request = SearchRequest.builder()
                .facet("categories")
                .build();
        assertTrue(request.facets.isEmpty());
    }

    @Test
    void builder_facets_replacesFacets() {
        SearchRequest request = SearchRequest.builder()
                .facet("categories", "combat")
                .facets(List.of(List.of("loaders:fabric")))
                .build();
        assertEquals(1, request.facets.size());
        assertEquals(List.of("loaders:fabric"), request.facets.get(0));
    }

    @Test
    void builder_facets_skipsNullAndEmptyGroups() {
        SearchRequest request = SearchRequest.builder()
                .facets(java.util.Arrays.asList(null, List.of(), List.of("a:b")))
                .build();
        assertEquals(1, request.facets.size());
    }
}