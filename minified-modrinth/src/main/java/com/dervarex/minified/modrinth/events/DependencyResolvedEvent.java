package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.modrinth.versions.Version;
import com.dervarex.minified.modrinth.versions.VersionDependency;
import com.dervarex.minified.modrinth.versions.VersionStatus;
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

    /**
     * Mod authors set dependencies to exact versions, which can get archived or unlisted.
     * They still download, but a launcher might want to say "this mod uses an archived version of X" or smth to the User.
     *
     * @return true if Modrinth reports a status other than {@link VersionStatus#LISTED} for the resolved version,
     * false if it's listed or modrinth refused to tell us
     */
    @API(status = API.Status.EXPERIMENTAL)
    public boolean notListed() {
        VersionStatus status = resolved.getStatus();
        return status != null && status != VersionStatus.LISTED;
    }
}
