/**
 * Public Modrinth version models and endpoints. Get
 * {@link com.dervarex.minified.modrinth.versions.VersionsClient} from
 * {@link com.dervarex.minified.modrinth.Modrinth#versions()}.
 *
 * <h2>Getting versions</h2>
 * <pre>{@code
 * Version version = modrinth.versions().get("version-id");
 * List<Version> versions = modrinth.versions().getMany("version-id-1", "version-id-2");
 * List<Version> ofProject = modrinth.versions().getByProject("sodium");
 * Version byNumber = modrinth.versions().getByNumber("sodium", "mc1.21.11-0.8.0");
 * }</pre>
 *
 * <h2>Filtering</h2>
 * Pass {@link com.dervarex.minified.modrinth.VersionSearchOptions} to only get some versions of a project:
 * <pre>{@code
 * VersionSearchOptions options = VersionSearchOptions.builder()
 *         .gameVersions("1.21.11")
 *         .loaders(ModLoader.FABRIC)
 *         .featured(true)
 *         .build();
 *
 * List<Version> versions = modrinth.versions().getByProject("sodium", options);
 * Version latest = modrinth.versions().getLatest("sodium", options);
 * }</pre>
 * {@code getLatest} returns {@code null} when nothing matches.
 *
 * <h2>By file hash</h2>
 * Find out which version a file belongs to, by its sha1 or sha512 hash:
 * <pre>{@code
 * Version version = modrinth.versions().fromHash("file-hash");
 * List<Version> versions = modrinth.versions().fromHashes("file-hash-1", "file-hash-2");
 * }</pre>
 *
 * <h2>Downloading</h2>
 * <pre>{@code
 * Path jar = version.download(modsFolder);
 * List<Path> dependencies = version.downloadDependencies(modsFolder);
 * }</pre>
 * {@link com.dervarex.minified.modrinth.versions.Version#resolveDependencies()} gives the dependencies as versions without downloading them.
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.versions;

import org.apiguardian.api.API;