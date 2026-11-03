/**
 * Search and retrieve projects such as mods, modpacks, resource packs, datapacks, and more. Get the
 * {@link com.dervarex.minified.modrinth.projects.ProjectsClient} from
 * {@link com.dervarex.minified.modrinth.Modrinth#projects()}.
 *
 * <h2>Getting projects</h2>
 * Get a project by its ID or slug, or several at once:
 * <pre>{@code
 * Project project = modrinth.projects().get("sodium");
 * List<Project> projects = modrinth.projects().getMany("sodium", "iris");
 * }</pre>
 *
 * <h2>Searching</h2>
 * <pre>{@code
 * SearchRequest request = SearchRequest.builder()
 *         .query("optimization")
 *         .loaders(ModLoader.FABRIC)
 *         .limit(20)
 *         .build();
 *
 * SearchResult<Project> result = modrinth.projects().search(request);
 * }</pre>
 *
 * <h2>Checking a project</h2>
 * <pre>{@code
 * if (project.hasLoader(ModLoader.FABRIC) && project.supportsGameVersion("1.21.11")) {
 *     // the project supports Fabric with Minecraft 1.21.11
 * }
 *
 * boolean optimization = project.hasCategory("optimization");
 * boolean clientOnly = project.getEnvironment().contains(Environment.CLIENT_ONLY);
 * GalleryImage featured = project.getFeaturedImage();
 * }</pre>
 *
 * <h2>Versions of a project</h2>
 * Versions come from {@link com.dervarex.minified.modrinth.Modrinth#versions()}:
 * <pre>{@code
 * VersionSearchOptions options = VersionSearchOptions.builder()
 *         .gameVersions("1.21.11")
 *         .loaders(ModLoader.FABRIC)
 *         .build();
 *
 * Version latest = modrinth.versions().getLatest(project.getId(), options);
 * }</pre>
 * @see com.dervarex.minified.modrinth.versions
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.projects;

import org.apiguardian.api.API;