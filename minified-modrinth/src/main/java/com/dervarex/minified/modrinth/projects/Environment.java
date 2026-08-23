package com.dervarex.minified.modrinth.projects;

/**
 * Replaces clientSide/serverSide as the preferred way to describe where a project or version runs.
 */
public enum Environment {
    CLIENT_AND_SERVER("client_and_server"),
    CLIENT_ONLY("client_only"),
    CLIENT_ONLY_SERVER_OPTIONAL("client_only_server_optional"),
    SINGLEPLAYER_ONLY("singleplayer_only"),
    SERVER_ONLY("server_only"),
    SERVER_ONLY_CLIENT_OPTIONAL("server_only_client_optional"),
    DEDICATED_SERVER_ONLY("dedicated_server_only"),
    CLIENT_OR_SERVER("client_or_server"),
    CLIENT_OR_SERVER_PREFERS_BOTH("client_or_server_prefers_both"),
    UNKNOWN("unknown");

    private final String apiValue;

    Environment(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static Environment fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (Environment environment : values()) {
            if (environment.apiValue.equalsIgnoreCase(value)) {
                return environment;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return apiValue;
    }
}
