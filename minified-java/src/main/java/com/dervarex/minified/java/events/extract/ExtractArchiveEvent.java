package com.dervarex.minified.java.events.extract;

import com.dervarex.minified.events.Event;

public record ExtractArchiveEvent(ArchiveType archiveType, int progress) implements Event { }