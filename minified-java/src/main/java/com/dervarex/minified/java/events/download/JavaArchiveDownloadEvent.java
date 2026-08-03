package com.dervarex.minified.java.events.download;

import com.dervarex.minified.events.Event;

public record JavaArchiveDownloadEvent(
        double progress,
        long downloadedBytes,
        long totalBytes,
        String downloadUrl
) implements Event { }