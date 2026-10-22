package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;

public record NonZeroExitCodeEvent(int exitCode) implements Event {
}
