package com.dervarex.minified.utils.download;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class DownloadProgress {

    private final AtomicLong downloaded = new AtomicLong();
    private final long totalBytes;
    private final Consumer<Double> consumer;

    public DownloadProgress(long totalBytes, Consumer<Double> consumer) {
        this.totalBytes = totalBytes;
        this.consumer = consumer;
    }

    public void addBytes(long bytes) {
        consumer.accept(downloaded.addAndGet(bytes) / (double) totalBytes);
    }
}