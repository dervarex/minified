package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

import java.nio.file.Path;

/**
 * Posted when a file already exists at the download destination but its hash doesn't match,
 * it will get downloaded again and replaced
 *
 * @param filename     the name of the file
 * @param destination  the file that gets replaced
 * @param algorithm    the hash algorithm that was checked, with the name Modrinth uses
 * @param expectedHash the hash Modrinth reports
 * @param actualHash   the hash of the existing file
 */
@API(status = API.Status.EXPERIMENTAL)
public record OutdatedFileReplacedEvent(
        String filename,
        Path destination,
        String algorithm,
        String expectedHash,
        String actualHash
) implements Event {
}
