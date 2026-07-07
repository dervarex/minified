package com.dervarex.minified.modrinth.versions;

public enum VersionType {
    RELEASE("release"),
    BETA("beta"),
    ALPHA("alpha");

    private final String apiValue;

    VersionType(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static VersionType fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (VersionType type : values()) {
            if (type.apiValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown version type: " + value);
    }

    @Override
    public String toString() {
        return apiValue;
    }
}

