package com.dervarex.minified.modrinth.versions;

/**
 * The release channel of a version
 */
public enum VersionType {
    RELEASE("release"),
    BETA("beta"),
    ALPHA("alpha"),
    UNKNOWN("unknown");

    private final String apiValue;

    VersionType(String apiValue) {
        this.apiValue = apiValue;
    }

    /**
     * {@return the value the Modrinth API uses}
     */
    public String getApiValue() {
        return apiValue;
    }

    /**
     * Finds the constant for a value of the Modrinth API, ignores casing
     *
     * @param value the API value
     * @return the constant, {@link #UNKNOWN} for values this library doesn't know yet, {@code null} for {@code null}
     */
    public static VersionType fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (VersionType type : values()) {
            if (type.apiValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
