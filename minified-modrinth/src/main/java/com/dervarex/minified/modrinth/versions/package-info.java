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
 * Version latest = modrinth.versions().resolveLatest("sodium", options);
 * Version byNumber = modrinth.versions().getVersionByNumber("sodium", "mc1.21.11-0.8.0");
 * }</pre>
 * {@code resolveLatest} and {@code getVersionByNumber} return {@code null} when nothing matches
 *
 * <h2>By file hash</h2>
 * Find out which version a file belongs to, by its sha1 or sha512 hash:
 * <pre>{@code
 * Version version = modrinth.versions().fromHash("file-hash");
 * List<Version> versions = modrinth.versions().fromHashes("file-hash-1", "file-hash-2");
 * }</pre>
 *
 * <h2>Dependencies</h2>
 * <pre>{@code
 * List<VersionDependency> dependencies = modrinth.versions().dependencies("version-id");
 * }</pre>
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.versions;

import org.apiguardian.api.API;