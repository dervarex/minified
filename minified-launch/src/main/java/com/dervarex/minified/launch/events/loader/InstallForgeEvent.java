package com.dervarex.minified.launch.events.loader;

import com.dervarex.minified.events.Event;

/**
 * Fired continuously while Minecraft Forge is installing
 *
 * @param stage         current installation phase of the Forge installer process
 * @param gameVersion   target Minecraft version (e.g., {@code 1.20.1})
 * @param loaderVersion Forge loader version being installed (e.g., {@code 47.2.0})
 */
public record InstallForgeEvent(
        Stage stage,
        String gameVersion,
        String loaderVersion
) implements Event {

    public enum Stage {
        /** Initial environment checks and directory setup */
        PREPARING,
        /** Fetching required installer binaries */
        DOWNLOADING_INSTALLER,
        /** Unpacking Forge libraries into the target directory */
        EXTRACTING,
        /** Executing the Forge installer process */
        RUNNING_INSTALLER,
        /** Updating the client launcher profile configuration */
        WRITING_PROFILE,
        /** Installation completed successfully */
        FINISHED,
        /** Installation encountered an error or was aborted */
        FAILED
    }
}