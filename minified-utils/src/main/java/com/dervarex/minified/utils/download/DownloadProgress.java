package com.dervarex.minified.utils.download;

import org.apiguardian.api.API;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Recently moved over from Launch module, be careful.
 */
@API(status = API.Status.EXPERIMENTAL)
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