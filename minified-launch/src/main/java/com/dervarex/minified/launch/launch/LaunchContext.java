package com.dervarex.minified.launch.launch;

import com.dervarex.minified.auth.user.User;
import com.dervarex.minified.events.EventBus;
import lombok.Getter;
import lombok.Setter;
import org.apiguardian.api.API;

import java.io.IOException;

/**
 * Internal Launching Context.
 */
@API(status = API.Status.INTERNAL)
public final class LaunchContext {

    @Getter
    private final User user;
    @Getter
    private final LaunchConfiguration launchConfiguration;
    @Getter
    private final EventBus eventBus;

    @Getter
    @Setter
    private boolean online;

    // stopping, see requestStop
    private final Thread launchThread = Thread.currentThread();
    private Process process;
    private boolean interruptible;
    private boolean finished;
    private volatile boolean stopRequested;

    public LaunchContext(User user, LaunchConfiguration launchConfiguration) {
        this.user = user;
        this.launchConfiguration = launchConfiguration;
        this.eventBus = launchConfiguration.getEventBus();
    }

    /**
     * Stops the launch and whatever it's doing right now
     */
    public synchronized void requestStop(boolean force) {
        if (finished) {
            return;
        }
        stopRequested = true;
        if (process != null) {
            if (force) {
                process.destroyForcibly();
            } else {
                process.destroy();
            }
        } else if (interruptible) {
            launchThread.interrupt();
        }
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    /**
     * @throws StopRequestedException if the launch should stop before the game was started
     */
    public void throwIfStopRequested() {
        if (stopRequested) {
            throw new StopRequestedException();
        }
    }

    /**
     * Runs a step that may be interrupted when the launch is stopped without leaving behind Problems. Installing Java or Forge can't be interupted
     */
    public void runInterruptibly(Runnable step) {
        synchronized (this) {
            throwIfStopRequested();
            interruptible = true;
        }
        try {
            step.run();
        } finally {
            synchronized (this) {
                interruptible = false;
                if (stopRequested) {
                    Thread.interrupted();
                }
            }
        }
        throwIfStopRequested();
    }

    /**
     * Starts the game, unless the launch was stopped already
     */
    public synchronized Process startProcess(ProcessBuilder processBuilder) throws IOException {
        throwIfStopRequested();
        process = processBuilder.start();
        return process;
    }

    public synchronized boolean isProcessStarted() {
        return process != null;
    }

    /**
     * Ignore stop requests from now on, the launch is over
     */
    public synchronized void finish() {
        finished = true;
    }

    /**
     * Thrown at a checkpoint when the launch was stopped, {@link Launcher#launchMinecraft} catches it
     */
    public static final class StopRequestedException extends RuntimeException {
        StopRequestedException() {
            super("The launch was stopped", null, false, false);
        }
    }
}
