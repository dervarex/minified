package com.dervarex.minified.launch.launch.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

// the "games" here are shell scripts, so no windows
@EnabledOnOs({OS.LINUX, OS.MAC})
class HeadlessWatcherTest {

    private final HeadlessWatcher watcher = new HeadlessWatcher(new String[]{"Sound engine started"}, 10, TimeUnit.MILLISECONDS.toNanos(200));

    @Test
    void stopsTheGameOnceItReachedTheMenu() throws Exception {
        Process game = game("echo Loading; echo 'Sound engine started'; sleep 30");

        assertEquals(0, watcher.watch(game));
        assertFalse(game.isAlive());
    }

    @Test
    void crashesAreFailures() throws Exception {
        assertNotEquals(0, watcher.watch(game("echo '#@!@# Game crashed!'; sleep 30")));
    }

    @Test
    void forgesFakeCrashReportIsNotACrash() throws Exception {
        Process game = game("""
                echo '---- Minecraft Crash Report ----'
                echo '// THIS IS NOT A ERROR'
                echo ''
                echo 'Time: today'
                echo 'Description: Loading screen debug info'
                echo 'Sound engine started'
                sleep 30
                """);

        assertEquals(0, watcher.watch(game));
    }

    @Test
    void realCrashReportsAreCrashesEvenAfterTheMarker() throws Exception {
        Process game = game("""
                echo '---- Minecraft Crash Report ----'
                echo '// Who set us up the TNT?'
                echo ''
                echo 'Time: today'
                echo 'Description: Rendering overlay'
                echo 'java.lang.NullPointerException'
                echo 'Sound engine started'
                sleep 30
                """);

        assertNotEquals(0, watcher.watch(game));
    }

    @Test
    void givesUpAfterTheTimeout() throws Exception {
        HeadlessWatcher impatient = new HeadlessWatcher(new String[]{"Sound engine started"}, 1, TimeUnit.MILLISECONDS.toNanos(200));

        assertEquals(124, impatient.watch(game("echo 'still loading'; sleep 30")));
    }

    @Test
    void quittingBeforeTheMarkerIsAFailure() throws Exception {
        assertNotEquals(0, watcher.watch(game("echo 'bye'; exit 0")));
    }

    private static Process game(String script) throws IOException {
        return new ProcessBuilder("sh", "-c", script).redirectErrorStream(true).start();
    }
}
