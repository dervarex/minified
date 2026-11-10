package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

import java.nio.file.Path;

/**
 * Posted after a Modrinth file was downloaded and checked successfully, or it was already there. <br>
 * Failed downloads throw an exception
 *
 * @param filename       the name of the file
 * @param url            where the file was downloaded from
 * @param destination    where the file is saved
 * @param alreadyPresent true if the file already existed with a matching hash
 */
@API(status = API.Status.STABLE)
public record DownloadFinishedEvent(String filename, String url, Path destination, boolean alreadyPresent) implements Event {
}
