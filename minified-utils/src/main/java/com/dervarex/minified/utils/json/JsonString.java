package com.dervarex.minified.utils.json;

/**
 * A JSON string
 *
 * @param value the value
 */
public record JsonString(String value) implements JsonValue {
    /**
     * @param value the value, null becomes empty
     */
    public JsonString(String value) {
        this.value = value == null ? "" : value;
    }

    /**
     * {@return {@link JsonType#STRING}}
     */
    @Override
    public JsonType getType() {
        return JsonType.STRING;
    }

    /**
     * {@return the value}
     */
    @Override
    public String asString() {
        return value;
    }

    /**
     * {@return the value escaped and in quotes}
     */
    @Override
    public String toJson() {
        return "\"" + JsonWriter.escape(value) + "\"";
    }

    /**
     * {@return the value, without quotes}
     */
    @Override
    public String toString() {
        return value;
    }
}

