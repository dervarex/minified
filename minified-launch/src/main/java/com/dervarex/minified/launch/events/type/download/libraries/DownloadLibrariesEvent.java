package com.dervarex.minified.launch.events.type.download.libraries;

import com.dervarex.minified.launch.events.Event;

public record DownloadLibrariesEvent(
        double progress,
        long downloadedBytes,
        long totalBytes,
        String currentFile,
        long currentFileBytes,
        long currentFileSize
) implements Event {}