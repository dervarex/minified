package com.dervarex.minified.launch.events.download.assets;

import com.dervarex.minified.events.Event;

public record DownloadAssetsEvent(
        double progress,
        long downloadedBytes,
        long totalBytes,
        String currentFile,
        long currentFileBytes,
        long currentFileSize
) implements Event {}