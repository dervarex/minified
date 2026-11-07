package com.dervarex.minified.utils.json;

/**
 * A JSON boolean
 *
 * @param value the value
 */
public record JsonBoolean(boolean value) implements JsonValue {

    /**
     * {@return {@link JsonType#BOOLEAN}}
     */
    @Override
    public JsonType getType() {
        return JsonType.BOOLEAN;
    }

    /**
     * {@return the value}
     */
    @Override
    public boolean asBoolean() {
        return value;
    }

    /**
     * {@return true or false}
     */
    @Override
    public String toJson() {
        return value ? "true" : "false";
    }

    /**
     * {@return same as {@link #toJson()}}
     */
    @Override
    public String toString() {
        return toJson();
    }
}

