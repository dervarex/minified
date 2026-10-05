package com.dervarex.minified.launch.events.download.client;

import com.dervarex.minified.events.Event;

/**
 * Gets fired while the Minecraft client JAR is being downloaded
 *
 * @param progress        the overall download progress as a value from <b>0.0</b> to <b>1.0</b>
 * @param downloadedBytes the number of bytes downloaded so far
 * @param totalBytes      the total size of the client JAR in bytes
 */
public record DownloadClientJarEvent(
        double progress,
        long downloadedBytes,
        long totalBytes
) implements Event { }