package com.dervarex.minified.launch.events.loader;

import com.dervarex.minified.events.Event;

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