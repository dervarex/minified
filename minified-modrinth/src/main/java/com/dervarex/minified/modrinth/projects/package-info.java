/**
 * Search and retrieve projects such as mods, modpacks, resource packs, datapacks, and more. Get the
 * {@link com.dervarex.minified.modrinth.projects.ProjectsClient} from
 * {@link com.dervarex.minified.modrinth.Modrinth#projects()}.
 *
 * <h2>Getting projects</h2>
 * Get a project by its ID or name, or several by their IDs:
 * <pre>{@code
 * Project project = modrinth.projects().get("sodium");
 * List<Project> projects = modrinth.projects().getMany("AANobbMI", "gvQqBUqZ");
 * }</pre>
 * Projects you get from a client are attached to the Client, so methods that need the API, like
 * {@link com.dervarex.minified.modrinth.projects.Project#getLatestVersion(com.dervarex.minified.modrinth.VersionSearchOptions)},
 * work. On a {@code Project} you created yourself they throw an
 * {@link com.dervarex.minified.modrinth.exceptions.ModrinthStateException}
 *
 * <h2>Searching</h2>
 * <pre>{@code
 * SearchRequest request = SearchRequest.builder()
 *         .query("optimization")
 *         .limit(20)
 *         .build();
 *
 * SearchResult<Project> result = modrinth.projects().search(request);
 * }</pre>
 *
 * <h2>Checking a project</h2>
 * <pre>{@code
 * if (project.hasLoader(ModLoader.FABRIC) && project.supportsVersion("1.21.11")) {
 *     // the project supports Fabric with Minecraft 1.21.11
 * }
 *
 * boolean optimization = project.hasTag("optimization");
 * boolean clientOnly = project.supportsEnvironment(Environment.CLIENT_ONLY);
 * boolean usesTelemetry = project.hasDisclosure(DisclosureType.TELEMETRY);
 *
 * GalleryImage featured = project.getFeaturedGallery();
 * }</pre>
 *
 * <h2>Versions of a project</h2>
 * <pre>{@code
 * VersionSearchOptions options = VersionSearchOptions.builder()
 *         .gameVersions("1.21.11")
 *         .loaders(ModLoader.FABRIC)
 *         .build();
 *
 * List<Version> versions = modrinth.projects().getVersions("sodium", options);
 * Version latest = modrinth.projects().getLatestVersion("sodium", "1.21.11", ModLoader.FABRIC);
 * Version latestOfProject = project.getLatestVersion("1.21.11", ModLoader.FABRIC);
 * }</pre>
 * @see com.dervarex.minified.modrinth.versions
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.projects;

import org.apiguardian.api.API;