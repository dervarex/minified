package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

import java.nio.file.Path;

/**
 * Posted right before a Modrinth file starts downloading. Not posted when the file is already there with a
 * matching hash, only {@link DownloadFinishedEvent} is posted then.
 *
 * @param filename    the name of the file
 * @param url         where the file gets downloaded from
 * @param destination where the file will be saved
 * @param totalBytes  the size of the file in bytes as Modrinth reports it, or <b>-1</b> if unknown
 */
@API(status = API.Status.STABLE)
public record DownloadStartedEvent(String filename, String url, Path destination, long totalBytes) implements Event {
}
