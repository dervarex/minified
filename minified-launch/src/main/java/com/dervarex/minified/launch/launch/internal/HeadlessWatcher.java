package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import org.apiguardian.api.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Watches the log of a headless game until it reached the main menu (or crashed), then stops it
 */
@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public final class HeadlessWatcher {
    // only logged once the game window exists, stuff like "Backend library: LWJGL" also show up in crash reports
    // 1.13 - 1.15 don't log the resource reload, and without a sound device there is no sound engine either,
    // the texture atlases get built in every version, that seems usable
    private static final String DEFAULT_HEADLESS_MARKERS =
            "Reloading ResourceManager,Sound engine started,textures-atlas,.png-atlas";

    private static final String CRASH_REPORT_HEADER = "---- Minecraft Crash Report ----";

    // forge 1.7.10 - 1.11 print a crash report on purpose to log the computer specs ("THIS IS NOT A ERROR")
    private static final String FAKE_CRASH_REPORT_DESCRIPTION = "Description: Loading screen debug info";

    // the description comes a few lines after the header (header, joke, empty line, time, description)
    private static final int CRASH_REPORT_DESCRIPTION_LINES = 6;

    private static final List<String> HEADLESS_CRASH_MARKERS = List.of(
            CRASH_REPORT_HEADER,
            "#@!@# Game crashed!",
            "Exception in thread \"main\""
    );

    // logged by LWJGL 2 (with org.lwjgl.util.Debug) when it creates the window, the only thing we get from a pre 1.6
    private static final String LEGACY_HEADLESS_MARKER = "[LWJGL] Pixel format info";

    private static final long HEADLESS_GRACE_PERIOD_NANOS = TimeUnit.SECONDS.toNanos(3);

    // the window marker comes before the game loaded anything, so it has to survive a while after it
    private static final long LEGACY_HEADLESS_GRACE_PERIOD_NANOS = TimeUnit.SECONDS.toNanos(10);

    private final String[] markers;
    private final long timeoutSeconds;
    private final long gracePeriodNanos;

    HeadlessWatcher(String[] markers, long timeoutSeconds, long gracePeriodNanos) {
        this.markers = markers;
        this.timeoutSeconds = timeoutSeconds;
        this.gracePeriodNanos = gracePeriodNanos;
    }

    /**
     * @param logsNoMarkers true for versions before 1.6, they get the LWJGL window marker
     */
    public static HeadlessWatcher forLaunch(LaunchConfiguration launchConfig, boolean logsNoMarkers) {
        boolean legacyMarker = launchConfig.getHeadlessMarker() == null && logsNoMarkers;
        String markersRaw = launchConfig.getHeadlessMarker() != null
                ? launchConfig.getHeadlessMarker()
                : legacyMarker ? LEGACY_HEADLESS_MARKER : DEFAULT_HEADLESS_MARKERS;
        long gracePeriod = legacyMarker ? LEGACY_HEADLESS_GRACE_PERIOD_NANOS : HEADLESS_GRACE_PERIOD_NANOS;
        return new HeadlessWatcher(markersRaw.split(","), launchConfig.getHeadlessTimeoutSeconds(), gracePeriod);
    }

    /**
     * Waits until the game logs one of the headless markers, then stops it
     *
     * @return 0 if a marker was reached without the game crashing, 124 on timeout, otherwise a non-zero exit code
     */
    public int watch(Process process) throws InterruptedException {
        long deadline = System.nanoTime() + timeoutSeconds * 1_000_000_000L;

        AtomicLong matchedAt = new AtomicLong(-1);
        AtomicBoolean crashed = new AtomicBoolean(false);

        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                // lines left until we know if a crash report header belongs to a real crash or ist just forge pretending again
                int crashReportLinesLeft = -1;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (crashReportLinesLeft >= 0) {
                        if (line.contains(FAKE_CRASH_REPORT_DESCRIPTION)) {
                            crashReportLinesLeft = -1;
                        } else if (line.contains("Description: ") || crashReportLinesLeft-- == 0) {
                            // any other description means it's real
                            crashed.set(true);
                            crashReportLinesLeft = -1;
                        }
                    }
                    for (String crashMarker : HEADLESS_CRASH_MARKERS) {
                        if (!line.contains(crashMarker)) {
                            continue;
                        }
                        if (crashMarker.equals(CRASH_REPORT_HEADER)) {
                            crashReportLinesLeft = CRASH_REPORT_DESCRIPTION_LINES;
                        } else {
                            crashed.set(true);
                        }
                    }
                    if (matchedAt.get() < 0 && !crashed.get()) {
                        for (String marker : markers) {
                            if (line.contains(marker.trim())) {
                                matchedAt.set(System.nanoTime());
                                break;
                            }
                        }
                    }
                }
                if (crashReportLinesLeft >= 0) {
                    crashed.set(true);
                }
            } catch (IOException ignored) {
            }
        }, "minified-headless-log-reader");
        readerThread.setDaemon(true);
        readerThread.start();

        boolean stoppedByUs = false;
        while (process.isAlive()) {
            if (crashed.get()) {
                System.err.println("[minified-headless] game crashed, stopping process");
                terminateProcess(process);
                break;
            }
            long matched = matchedAt.get();
            // keep watching for a crash for a moment after the marker, then stop the game
            if (matched >= 0 && System.nanoTime() - matched > gracePeriodNanos) {
                stoppedByUs = true;
                terminateProcess(process);
                break;
            }
            if (matched < 0 && System.nanoTime() > deadline) {
                System.err.println("[minified-headless] timeout after "
                        + timeoutSeconds + "s, killing process");
                process.destroyForcibly();
                process.waitFor();
                return 124;
            }
            process.waitFor(500, TimeUnit.MILLISECONDS);
        }

        process.waitFor();
        readerThread.join(TimeUnit.SECONDS.toMillis(5));

        int exitCode = process.exitValue();
        if (crashed.get()) {
            return exitCode != 0 ? exitCode : 1;
        }
        if (matchedAt.get() >= 0) {
            if (stoppedByUs) {
                return 0;
            }
            // nothing closes the game in headless mode, so it died during the grace period
            System.err.println("[minified-headless] game exited right after reaching a marker");
            return exitCode != 0 ? exitCode : 1;
        }
        System.err.println("[minified-headless] game exited before reaching a marker");
        return exitCode != 0 ? exitCode : 1;
    }

    private static void terminateProcess(Process process) {
        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                System.err.println("[minified-headless] graceful stop timed out, forcing kill");
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }
}
