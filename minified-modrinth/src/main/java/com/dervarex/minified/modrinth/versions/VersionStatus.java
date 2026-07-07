package com.dervarex.minified.modrinth.versions;

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

    public String getApiValue() {
        return apiValue;
    }

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

    @Override
    public String toString() {
        return apiValue;
    }
}

