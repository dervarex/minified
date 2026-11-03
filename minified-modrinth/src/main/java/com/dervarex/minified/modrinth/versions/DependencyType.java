package com.dervarex.minified.modrinth.versions;

/**
 * Dependency Types, how they depend on each other
 */
public enum DependencyType {
    REQUIRED("required"),
    OPTIONAL("optional"),
    EMBEDDED("embedded"),
    INCOMPATIBLE("incompatible"),
    UNKNOWN("unknown");

    private final String apiValue;

    DependencyType(String apiValue) {
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
     * Finds the constant for a value of the Modrinth API, ignores casing
     *
     * @param value the API value
     * @return the constant, {@link #UNKNOWN} for values this library doesn't know yet, {@code null} for {@code null}
     */
    public static DependencyType fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (DependencyType type : values()) {
            if (type.apiValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}

