package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

/**
 * Gets fired while a Modrinth file is being downloaded, throttled to 100ms, and once when it's finished.
 *
 * @param filename        the name of the file
 * @param url             where the file is being downloaded from
 * @param progress        the download progress as a value from <b>0.0</b> to <b>1.0</b>, or <b>-1.0</b> if the size is unknown
 * @param downloadedBytes the number of bytes downloaded so far
 * @param totalBytes      the total size of the file in bytes, or <b>-1</b> if unknown
 */
@API(status = API.Status.EXPERIMENTAL)
public record DownloadProgressEvent(
        String filename,
        String url,
        double progress,
        long downloadedBytes,
        long totalBytes
) implements Event {
}
