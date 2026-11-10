package com.dervarex.minified.java.events.extract;

import org.apiguardian.api.API;

/**
 * The format of the archive that's being extracted
 */
@API(status = API.Status.STABLE)
public enum ArchiveType {
    /** The archive is a ZIP file */
    zip,
    /** The archive is a gzipped tarball */
    tarGz
}
