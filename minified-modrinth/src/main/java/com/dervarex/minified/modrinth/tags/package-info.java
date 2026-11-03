/**
 * The lists Modrinth uses for projects and to filter searches, like categories, loaders and game versions. Get the
 * {@link com.dervarex.minified.modrinth.tags.TagsClient} from {@link com.dervarex.minified.modrinth.Modrinth#tags()}.
 * <pre>{@code
 * List<Category> categories = modrinth.tags().categories();
 * List<GameVersion> gameVersions = modrinth.tags().gameVersions();
 *
 * for (LoaderTag loader : modrinth.tags().loaders()) {
 *     System.out.println(loader.getName() + ": " + loader.getSupportedProjectTypes());
 * }
 * }</pre>
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.tags;

import org.apiguardian.api.API;