package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.modrinth.versions.Version;
import com.dervarex.minified.modrinth.versions.VersionDependency;
import org.apiguardian.api.API;

/**
 * Posted while resolving dependencies, when a dependency is left out. Most of these are fine, but
 * {@link Reason#INCOMPATIBLE} and {@link Reason#UNRESOLVABLE} should be shown to the User, might be a Problem.
 *
 * @param parent     the version that has this dependency
 * @param dependency the dependency as Modrinth lists it on the parent
 * @param reason     why it was left out
 */
@API(status = API.Status.EXPERIMENTAL)
public record DependencySkippedEvent(Version parent, VersionDependency dependency, Reason reason) implements Event {

    @API(status = API.Status.EXPERIMENTAL)
    public enum Reason {
        /** optional dependency, when optional ones weren't asked for */
        OPTIONAL,
        /** the mod must <b>not</b> be installed together with the parent, so the launcher should warn if it is */
        INCOMPATIBLE,
        /** already bundled inside the parent's jar */
        EMBEDDED,
        /** no version could be found, for example none matches the parent's game versions and loaders */
        UNRESOLVABLE,
        /** points back to a version that's already being resolved further up (a -> b -> a) */
        CIRCULAR,
        /** Modrinth sent a dependency type this library doesn't know yet, just to be safe */
        UNKNOWN_TYPE
    }
}
