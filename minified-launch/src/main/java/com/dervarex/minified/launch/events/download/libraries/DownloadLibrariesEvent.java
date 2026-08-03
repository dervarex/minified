package com.dervarex.minified.launch.events.download.libraries;

import com.dervarex.minified.events.Event;

public record DownloadLibrariesEvent(
        double progress,
        long downloadedBytes,
        long totalBytes,
        String currentFile,
        long currentFileBytes,
        long currentFileSize
) implements Event {}