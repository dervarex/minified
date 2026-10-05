package com.dervarex.minified.java.events.download;

import com.dervarex.minified.events.Event;

/**
 * Gets fired while a Java runtime archive is being downloaded
 *
 * @param progress        the overall download progress as a value from <b>0.0</b> to <b>1.0</b>
 * @param downloadedBytes the number of bytes downloaded so far
 * @param totalBytes      the total size of the Java archive in bytes
 * @param downloadUrl     the URL the Java archive is being downloaded from
 */
public record JavaArchiveDownloadEvent(
        double progress,
        long downloadedBytes,
        long totalBytes,
        String downloadUrl
) implements Event { }