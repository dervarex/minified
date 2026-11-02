package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.modrinth.versions.Version;
import com.dervarex.minified.modrinth.versions.VersionDependency;
import org.apiguardian.api.API;

/**
 * Posted while resolving dependencies, every time a dependency got turned into an actual version.
 * Each version is only posted once per resolve, even if several mods depend on it.
 *
 * @param parent     the version that has this dependency
 * @param dependency the dependency as Modrinth lists it on the parent
 * @param resolved   the version that will be used for it
 */
@API(status = API.Status.EXPERIMENTAL)
public record DependencyResolvedEvent(Version parent, VersionDependency dependency, Version resolved) implements Event {
}
