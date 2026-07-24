package com.dervarex.minified.launch.events.type.loader;

import com.dervarex.minified.launch.events.Event;

public record InstallNeoforgeEvent(
        Stage stage,
        String gameVersion,
        String loaderVersion
) implements Event {

    public enum Stage {
        PREPARING,
        DOWNLOADING_INSTALLER,
        RUNNING_INSTALLER,
        WRITING_PROFILE,
        FINISHED,
        FAILED
    }
}