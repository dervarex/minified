package com.dervarex.minified.launch.events.download.libraries;

import com.dervarex.minified.events.Event;

/**
 * Gets fired every time a new library file has been downloaded
 *
 * @param progress         the overall download progress as a value from <b>0.0</b> to <b>1.0</b>
 * @param downloadedBytes  the total number of bytes downloaded so far
 * @param totalBytes       the total size of all libraries in bytes
 * @param currentFile      the name or path of the library currently being downloaded
 * @param currentFileBytes the number of bytes downloaded for the current file
 * @param currentFileSize  the total size of the current file in bytes
 */
public record DownloadLibrariesEvent(
        double progress,
        long downloadedBytes,
        long totalBytes,
        String currentFile,
        long currentFileBytes,
        long currentFileSize
) implements Event {}