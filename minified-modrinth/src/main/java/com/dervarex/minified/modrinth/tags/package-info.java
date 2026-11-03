/**
 * Public tag and license models for the Modrinth API. Tags are the lists Modrinth uses for projects and
 * filter searches, like categories, loaders and game versions. Get the
 * {@link com.dervarex.minified.modrinth.tags.TagsClient} from {@link com.dervarex.minified.modrinth.Modrinth#tags()}.
 * <pre>{@code
 * List<Tag> categories = modrinth.tags().categories();
 * List<Tag> versions = modrinth.tags().versions();
 *
 * for (Tag loader : modrinth.tags().loaders()) {
 *     System.out.println(loader.name + ": " + loader.value);
 * }
 * }</pre>
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.tags;

import org.apiguardian.api.API;