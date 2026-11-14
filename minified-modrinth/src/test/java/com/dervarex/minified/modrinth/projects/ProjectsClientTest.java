package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.SearchRequest;
import com.dervarex.minified.modrinth.SearchResult;
import com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ProjectsClientTest {

    private final ProjectsClient projects = Modrinth.connect().projects();

    @Test
    @Tag("manual")
    void getsAProjectBySlug() {
        Project iris = projects.get("iris");

        assertEquals("YL57xq9U", iris.getId());
        assertEquals("Iris Shaders", iris.getTitle());
        assertEquals(ProjectType.MOD, iris.getProjectType());
        assertTrue(iris.hasLoader(ModLoader.FABRIC));
        assertNotNull(iris.getTeam());
    }

    @Test
    @Tag("manual")
    void getsManyProjectsInTheAskedOrder() {
        List<String> slugs = projects.getMany("AANobbMI", "iris").stream().map(Project::getSlug).toList();

        assertEquals(List.of("sodium", "iris"), slugs);
    }

    @Test
    @Tag("manual")
    void searchFindsIris() {
        SearchResult<Project> result = projects.search(SearchRequest.builder().query("iris shaders").limit(5).build());

        assertEquals(5, result.getLimit());
        Project iris = result.getHits().stream().filter(project -> "iris".equals(project.getSlug())).findFirst().orElseThrow();
        assertEquals("YL57xq9U", iris.getId());
        assertNotNull(iris.getTeam());
        assertFalse(iris.getLoaders().isEmpty());
    }

    @Test
    @Tag("manual")
    void unknownProjectsAreNotFound() {
        assertThrows(ModrinthNotFoundException.class, () -> projects.get("this-project-does-not-exist-hopefully-" + new Random().nextInt(10000))); // don't want anyone to troll us and actually create an project named like that
    }

    @Test
    void searchSendsTheRequestAndKeepsTheHitOrder() throws IOException {
        List<String> queries = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            String query = URLDecoder.decode(exchange.getRequestURI().getRawQuery(), StandardCharsets.UTF_8);
            queries.add(exchange.getRequestURI().getPath() + "?" + query);
            String body = exchange.getRequestURI().getPath().equals("/search")
                    ? "{\"hits\":[{\"project_id\":\"b\"},{\"project_id\":\"a\"}],\"offset\":20,\"limit\":2,\"total_hits\":99}"
                    // modrinth returns /projects in any order
                    : "[{\"id\":\"a\",\"slug\":\"first\"},{\"id\":\"b\",\"slug\":\"second\"}]";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try {
            ProjectsClient client = Modrinth.connect("http://localhost:" + server.getAddress().getPort()).projects();
            SearchResult<Project> result = client.search(SearchRequest.builder()
                    .query("x")
                    .index(SearchRequest.SearchIndex.DOWNLOADS)
                    .loaders(ModLoader.FABRIC)
                    .gameVersions("1.21.11")
                    .limit(2)
                    .offset(20)
                    .build());

            assertEquals(List.of("b", "a"), result.getHits().stream().map(Project::getId).toList());
            assertEquals(20, result.getOffset());
            assertEquals(2, result.getLimit());
            assertEquals(99, result.getTotalHits());
            assertEquals(List.of(
                    "/search?query=x&index=downloads&limit=2&offset=20&facets=[[\"categories:fabric\"],[\"versions:1.21.11\"]]",
                    "/projects?ids=[\"b\",\"a\"]"
            ), queries);
        } finally {
            server.stop(0);
        }
    }
}
