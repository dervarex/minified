package com.dervarex.minified.servers.events.download.server;

import com.dervarex.minified.events.Event;

public record DownloadServerJarEvent(
        double progress,
        long downloadedBytes,
        long totalBytes
) implements Event { }