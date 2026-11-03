package com.dervarex.minified.modrinth.projects;

/**
 * What a Project is, for example a shader
 */
public enum ProjectType {
    MOD("mod"),
    MODPACK("modpack"),
    RESOURCEPACK("resourcepack"),
    SHADER("shader"),
    PLUGIN("plugin"),
    DATAPACK("datapack"),
    MINECRAFT_JAVA_SERVER("minecraft_java_server"),
    UNKNOWN("unknown");

    private final String apiValue;

    ProjectType(String apiValue) {
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
    public static ProjectType fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (ProjectType type : values()) {
            if (type.apiValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
