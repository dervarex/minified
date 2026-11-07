package com.dervarex.minified.utils.json;

/**
 * JSON null, use {@link #INSTANCE}
 */
public final class JsonNull implements JsonValue {
    /**
     * The only instance
     */
    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    /**
     * {@return {@link JsonType#NULL}}
     */
    @Override
    public JsonType getType() { return JsonType.NULL; }

    /**
     * {@return null}
     */
    @Override
    public String toJson() { return "null"; }

    /**
     * {@return same as {@link #toJson()}}
     */
    @Override
    public String toString() { return toJson(); }
}

