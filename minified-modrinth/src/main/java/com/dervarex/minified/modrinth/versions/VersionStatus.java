package com.dervarex.minified.modrinth.versions;

/**
 * Whether a version is listed, unlisted, archived, a draft or scheduled
 */
public enum VersionStatus {
    LISTED("listed"),
    UNLISTED("unlisted"),
    ARCHIVED("archived"),
    DRAFT("draft"),
    SCHEDULED("scheduled"),
    UNKNOWN("unknown");

    private final String apiValue;

    VersionStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    /**
     * Gives the value the Modrinth API uses
     *
     * @return the API value
     */
    public String getApiValue() {
        return apiValue;
    }

    /**
     * Finds the constant for a value of the Modrinth API, ignores casing.
     *
     * @param value the API value
     * @return the constant, {@link #UNKNOWN} for values this library doesn't know yet, {@code null} for {@code null}
     */
    public static VersionStatus fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (VersionStatus status : values()) {
            if (status.apiValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}

