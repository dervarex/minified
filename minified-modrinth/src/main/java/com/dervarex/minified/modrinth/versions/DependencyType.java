package com.dervarex.minified.modrinth.versions;

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

    public String getApiValue() {
        return apiValue;
    }

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

    @Override
    public String toString() {
        return apiValue;
    }
}

