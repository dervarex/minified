package com.dervarex.minified.launch.events.type.download.assets;

import com.dervarex.minified.launch.events.Event;

public record DownloadAssetsEvent(
        double progress,
        long downloadedBytes,
        long totalBytes,
        String currentFile,
        long currentFileBytes,
        long currentFileSize
) implements Event {}