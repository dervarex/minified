package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.modrinth.versions.Version;
import org.apiguardian.api.API;

import java.nio.file.Path;

/**
 * Posted by {@link Version#downloadDependencies(Path)} after each dependency is downloaded, good for an
 * "installing dependencies 3/7" progress bar. For the bytes of a single file, use {@link DownloadProgressEvent}.
 *
 * @param root       the version whose dependencies are being downloaded
 * @param dependency the dependency that was just downloaded
 * @param file       where it was saved
 * @param completed  how many dependencies are done, including this one
 * @param total      how many dependencies there are in total
 */
@API(status = API.Status.STABLE)
public record DependencyDownloadedEvent(Version root, Version dependency, Path file, int completed, int total) implements Event {
}
