package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.SearchRequest;
import com.dervarex.minified.modrinth.SearchResult;
import com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.versions.Version;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectsClientTest {

    private final ProjectsClient projects = Modrinth.connect().projects();

    @Test
    void getsAProjectBySlug() {
        Project iris = projects.get("iris");

        assertEquals("YL57xq9U", iris.id);
        assertEquals("Iris Shaders", iris.title);
        assertEquals(ProjectType.MOD, iris.projectType);
        assertTrue(iris.hasLoader(ModLoader.FABRIC));
    }

    @Test
    void getsManyProjectsAtOnce() {
        List<String> slugs = projects.getMany("YL57xq9U", "AANobbMI").stream().map(project -> project.slug).sorted().toList();

        assertEquals(List.of("iris", "sodium"), slugs);
    }

    @Test
    void searchFindsIris() {
        SearchResult<Project> result = projects.search(SearchRequest.builder().query("iris shaders").limit(5).build());

        assertEquals(5, result.limit);
        assertTrue(Arrays.stream(result.getHits()).anyMatch(project -> "iris".equals(project.slug)));
    }

    @Test
    void latestVersionMatchesGameVersionAndLoader() {
        Version latest = projects.getLatestVersion("iris", "1.21.4", ModLoader.FABRIC);

        assertTrue(latest.supportsVersion("1.21.4"));
        assertTrue(latest.hasLoader(ModLoader.FABRIC));
    }

    @Test
    void unknownProjectsAreNotFound() {
        assertThrows(ModrinthNotFoundException.class, () -> projects.get("this-project-does-not-exist-hopefully-" + new Random().nextInt(10000))); // don't want anyone to troll us and actually create an project named like that
    }
}
