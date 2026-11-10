package com.dervarex.minified.launch.events.loader;

import com.dervarex.minified.events.Event;

/**
 * Fired for every message the Forge or NeoForge installer prints
 *
 * @param loader  the loader being installed in UPPERCASE, {@code FORGE} or {@code NEOFORGE}
 * @param message the message it printed
 */
public record LoaderInstallerOutputEvent(String loader, String message) implements Event {
}
