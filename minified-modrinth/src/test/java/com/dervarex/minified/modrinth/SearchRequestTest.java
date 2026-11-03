package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.loaders.ShaderLoader;
import com.dervarex.minified.modrinth.projects.Environment;
import com.dervarex.minified.modrinth.projects.ProjectType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchRequestTest {

    @Test
    void defaults() {
        SearchRequest request = SearchRequest.builder().build();
        assertNull(request.getQuery());
        assertEquals(SearchRequest.SearchIndex.RELEVANCE, request.getIndex());
        assertEquals(10, request.getLimit());
        assertEquals(0, request.getOffset());
        assertTrue(request.getFacets().isEmpty());
    }

    @Test
    void query_andIndex() {
        SearchRequest request = SearchRequest.builder()
                .query("sodium")
                .index(SearchRequest.SearchIndex.DOWNLOADS)
                .build();
        assertEquals("sodium", request.getQuery());
        assertEquals("downloads", request.getIndex().getApiValue());
    }

    @Test
    void index_mustNotBeNull() {
        assertThrows(NullPointerException.class, () -> SearchRequest.builder().index(null));
    }

    @Test
    void filters_becomeOneFacetGroupEach() {
        SearchRequest request = SearchRequest.builder()
                .projectTypes(ProjectType.MOD, ProjectType.SHADER)
                .environments(Environment.CLIENT_ONLY)
                .categories("optimization")
                .gameVersions("1.21.11", "1.21.10")
                .loaders(ModLoader.FABRIC, ShaderLoader.IRIS)
                .build();

        assertEquals(List.of(
                List.of("project_type:mod", "project_type:shader"),
                List.of("environment:client_only"),
                List.of("categories:optimization"),
                List.of("versions:1.21.11", "versions:1.21.10"),
                List.of("categories:fabric", "categories:iris")
        ), request.getFacets());
    }

    @Test
    void facet_dropsBlankNullAndDuplicateValues() {
        SearchRequest request = SearchRequest.builder()
                .facet("license", " mit ", null, "", "mit", "apache")
                .build();
        assertEquals(List.of(List.of("license:mit", "license:apache")), request.getFacets());
    }

    @Test
    void facet_withoutValues_addsNothing() {
        SearchRequest request = SearchRequest.builder()
                .facet("license")
                .categories((String[]) null)
                .loaders()
                .build();
        assertTrue(request.getFacets().isEmpty());
    }

    @Test
    void facets_cantBeChanged() {
        SearchRequest request = SearchRequest.builder().categories("a").build();
        assertThrows(UnsupportedOperationException.class, () -> request.getFacets().add(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> request.getFacets().getFirst().add("b"));
    }

    @Test
    void limit_andOffset() {
        SearchRequest request = SearchRequest.builder().limit(100).offset(30).build();
        assertEquals(100, request.getLimit());
        assertEquals(30, request.getOffset());
    }

    @Test
    void limit_outOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () -> SearchRequest.builder().limit(-1));
        assertThrows(IllegalArgumentException.class, () -> SearchRequest.builder().limit(SearchRequest.MAX_LIMIT + 1));
        assertThrows(IllegalArgumentException.class, () -> SearchRequest.builder().offset(-1));
    }

    @Test
    void builtRequest_doesNotChangeWithTheBuilder() {
        SearchRequest.Builder builder = SearchRequest.builder().categories("a");
        SearchRequest first = builder.build();
        builder.categories("b");
        assertEquals(1, first.getFacets().size());
        assertEquals(2, builder.build().getFacets().size());
    }
}
