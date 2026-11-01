package com.dervarex.minified.java.events.extract;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public enum ArchiveType {
    /** The archive is a ZIP file */
    zip,
    /** The archive is a gzipped tarball */
    tarGz
}
