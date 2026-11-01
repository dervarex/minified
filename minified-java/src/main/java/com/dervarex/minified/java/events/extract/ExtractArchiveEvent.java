package com.dervarex.minified.java.events.extract;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

/**
 * Gets fired while a downloaded Java archive is being extracted
 *
 * @param archiveType the archive format currently being extracted
 * @param progress    the extraction progress in percent, from <b>0</b> to <b>100</b>
 */
@API(status = API.Status.STABLE)
public record ExtractArchiveEvent(ArchiveType archiveType, int progress) implements Event { }