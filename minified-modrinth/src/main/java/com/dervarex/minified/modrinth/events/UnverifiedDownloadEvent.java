package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

import java.nio.file.Path;

/**
 * Posted when a Modrinth file is downloaded without checking its hash, because Modrinth only sent hashes in
 * algorithms this JVM doesn't know. The download still happens, though it's a good idea to tell the user.
 * This should never happen, since modrinth sends sha1 and sha256, but in case it does, here's the Event for it.
 *
 * @param filename    the name of the file
 * @param url         where the file was downloaded from
 * @param algorithm   the hash algorithm that couldn't be checked, with the name modrinth tells us
 * @param destination where the file was saved
 */
@API(status = API.Status.STABLE)
public record UnverifiedDownloadEvent(String filename, String url, String algorithm, Path destination) implements Event {
}
