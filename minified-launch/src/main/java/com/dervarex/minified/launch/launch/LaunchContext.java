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

    /** The user that plays */
    @Getter
    private final User user;
    /** What gets launched */
    @Getter
    private final LaunchConfiguration launchConfiguration;
    /** The event bus everything gets posted to */
    @Getter
    private final EventBus eventBus;

    /** Whether we launch online */
    @Getter
    @Setter
    private boolean online;

    // stopping, see requestStop
    private final Thread launchThread = Thread.currentThread();
    private Process process;
    private boolean interruptible;
    private boolean finished;
    private volatile boolean stopRequested;

    /**
     * Creates the context for one launch, call it on the thread that launches
     *
     * @param user the logged-in user, or null for offline mode
     * @param launchConfiguration the launch configuration
     */
    public LaunchContext(User user, LaunchConfiguration launchConfiguration) {
        this.user = user;
        this.launchConfiguration = launchConfiguration;
        this.eventBus = launchConfiguration.getEventBus();
    }

    /**
     * Stops the launch and whatever it's doing right now
     *
     * @param force true to kill the game instead of asking it to close
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

    /**
     * {@return true if the launch should stop}
     */
    public boolean isStopRequested() {
        return stopRequested;
    }

    /**
     * Checkpoint between launch steps
     *
     * @throws StopRequestedException if the launch should stop before the game was started
     */
    public void throwIfStopRequested() {
        if (stopRequested) {
            throw new StopRequestedException();
        }
    }

    /**
     * Runs a step that may be interrupted when the launch is stopped without leaving behind Problems. Installing Java or Forge can't be interupted
     *
     * @param step the step to run
     * @throws StopRequestedException if the launch was stopped before or during the step
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
     *
     * @param processBuilder the game's process builder
     * @throws StopRequestedException if the launch was stopped already
     * @throws IOException if the process can't be started
     * @return the game's process
     */
    public synchronized Process startProcess(ProcessBuilder processBuilder) throws IOException {
        throwIfStopRequested();
        process = processBuilder.start();
        return process;
    }

    /**
     * {@return true if the game process was started}
     */
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
