package com.dervarex.minified.modrinth.projects;

/**
 * Content disclosure categories a project can be tagged with; returned by the search endpoint only.
 */
public enum DisclosureType {
    AI_CONTENT("ai_content"),
    AI_CONTENT_CODE("ai_content_code"),
    AI_CONTENT_ASSETS("ai_content_assets"),
    AI_CONTENT_TEXT("ai_content_text"),
    AI_CONTENT_FUNCTIONALITY("ai_content_functionality"),
    ADVERTISEMENTS("advertisements"),
    EPILEPSY_TRIGGERS("epilepsy_triggers"),
    SYSTEM_INTERACTIONS("system_interactions"),
    TELEMETRY("telemetry"),
    TELEMETRY_OPT_IN("telemetry_opt_in"),
    TELEMETRY_OPT_OUT("telemetry_opt_out"),
    TELEMETRY_ALWAYS_ACTIVE("telemetry_always_active"),
    DERIVATIVE_WORK("derivative_work"),
    PAID_FEATURES("paid_features"),
    ARCHIVED("archived"),
    UNKNOWN("unknown");

    private final String apiValue;

    DisclosureType(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static DisclosureType fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (DisclosureType type : values()) {
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
