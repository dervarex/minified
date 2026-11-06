package com.dervarex.minified.utils.download;

import org.apiguardian.api.API;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Recently moved over from Launch module, be careful.
 */
@API(status = API.Status.STABLE)
public final class DownloadProgress {

    private final AtomicLong downloaded = new AtomicLong();
    private final long totalBytes;
    private final Consumer<Double> consumer;

    /**
     * @param totalBytes size of everything that gets downloaded
     * @param consumer receives the progress from 0 to 1
     */
    public DownloadProgress(long totalBytes, Consumer<Double> consumer) {
        this.totalBytes = totalBytes;
        this.consumer = consumer;
    }

    /**
     * Adds downloaded bytes and passes the new progress to the consumer
     *
     * @param bytes the bytes downloaded since the last call
     */
    public void addBytes(long bytes) {
        consumer.accept(downloaded.addAndGet(bytes) / (double) totalBytes);
    }
}