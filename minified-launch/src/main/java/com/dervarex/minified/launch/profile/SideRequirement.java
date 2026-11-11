package com.dervarex.minified.launch.profile;

import org.apiguardian.api.API;

import java.util.Locale;

/**
 * Whether a {@link ProfileFile} is needed on the client or on the server, same values as Modrinth's mrpack {@code env}
 */
@API(status = API.Status.STABLE)
public enum SideRequirement {
    /** The file is needed */
    REQUIRED,
    /** The file can be left out */
    OPTIONAL,
    /** The file doesn't work there */
    UNSUPPORTED;

    /**
     * {@return the lowercase name used in the profile JSON, for example {@code required}}
     */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * {@return the requirement for the name in the profile JSON}
     *
     * @param id the name, case doesn't matter
     * @throws IllegalArgumentException if there's no requirement with that name
     */
    public static SideRequirement fromId(String id) {
        return valueOf(id.toUpperCase(Locale.ROOT));
    }
}
