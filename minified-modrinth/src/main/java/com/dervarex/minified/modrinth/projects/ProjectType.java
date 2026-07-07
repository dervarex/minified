package com.dervarex.minified.modrinth.projects;

public enum ProjectType {
    MOD("mod"),
    MODPACK("modpack"),
    RESOURCEPACK("resourcepack"),
    SHADER("shader");

    private final String apiValue;

    ProjectType(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static ProjectType fromApiValue(String value) {
        if (value == null) {
            return null;
        }

        for (ProjectType type : values()) {
            if (type.apiValue.equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown project type: " + value);
    }

    @Override
    public String toString() {
        return apiValue;
    }
}