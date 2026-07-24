package com.dervarex.minified.launch.events.type.download.client;

import com.dervarex.minified.launch.events.Event;

public record DownloadClientJarEvent(
        double progress,
        long downloadedBytes,
        long totalBytes
) implements Event {
}