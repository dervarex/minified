package com.dervarex.minified.launch.events.download.client;

import com.dervarex.minified.events.Event;

public record DownloadClientJarEvent(
        double progress,
        long downloadedBytes,
        long totalBytes
) implements Event { }