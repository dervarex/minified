package com.dervarex.minified.modrinth.projects;

public enum SideSupport {
    REQUIRED("required"),
    OPTIONAL("optional"),
    UNSUPPORTED("unsupported"),
    UNKNOWN("unknown");

    private final String apiValue;

    SideSupport(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static SideSupport fromApiValue(String value) {
        if (value == null) {
            return null;
        }

        for (SideSupport support : values()) {
            if (support.apiValue.equalsIgnoreCase(value)) {
                return support;
            }
        }

        return UNKNOWN;
    }

    @Override
    public String toString() {
        return apiValue;
    }
}