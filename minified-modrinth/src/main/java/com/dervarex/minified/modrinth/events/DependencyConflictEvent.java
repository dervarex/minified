package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.modrinth.versions.Version;
import com.dervarex.minified.modrinth.versions.VersionDependency;
import org.apiguardian.api.API;

/**
 * Posted after resolving deps, when a version ends up in the result even though another version in it
 * says it's incompatible with it. That can happen when one dependency needs a mod another one is incompatible with.
 * Resolving still returns everything as usual, you'll decide if your Launcher refuses, warns or just doesn't care.
 * <p>
 * For reference, modrinth and prism don't check this at all (as of November 2026), they
 * read the {@code incompatible} dependency type, and only follow required dependencies and
 * install everything else silently. So ignoring this event isn't "bad", but showing a
 * harmless warning already does more than they do
 *
 * @see <a href="https://github.com/modrinth/code/blob/main/packages/modrinth-content-management/src/install/mod.rs">Modrinth App dependency resolver</a>
 * @see <a href="https://github.com/PrismLauncher/PrismLauncher/blob/develop/launcher/minecraft/mod/tasks/GetModDependenciesTask.cpp">Prism Launcher dependency task</a>
 *
 * @param declaredBy      the version that lists the incompatibility (the root version or one of its deps)
 * @param incompatibility the incompatible dependency as Modrinth lists it on {@code declaredBy}
 * @param conflicting     the version in the result that matches it
 */
@API(status = API.Status.EXPERIMENTAL)
public record DependencyConflictEvent(Version declaredBy, VersionDependency incompatibility, Version conflicting) implements Event {
}
