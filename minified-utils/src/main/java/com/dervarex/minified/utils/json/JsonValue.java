package com.dervarex.minified.utils.json;

/**
 * Any JSON value
 */
public interface JsonValue {
    /**
     * {@return the type}
     */
    JsonType getType();

    /**
     * {@return true if it's an object}
     */
    default boolean isObject() { return getType() == JsonType.OBJECT; }
    /**
     * {@return true if it's an array}
     */
    default boolean isArray() { return getType() == JsonType.ARRAY; }
    /**
     * {@return true if it's a string}
     */
    default boolean isString() { return getType() == JsonType.STRING; }
    /**
     * {@return true if it's a number}
     */
    default boolean isNumber() { return getType() == JsonType.NUMBER; }
    /**
     * {@return true if it's a boolean}
     */
    default boolean isBoolean() { return getType() == JsonType.BOOLEAN; }
    /**
     * {@return true if it's null}
     */
    default boolean isNull() { return getType() == JsonType.NULL; }

    /**
     * {@return this as an object}
     *
     * @throws IllegalStateException if it isn't one
     */
    default JsonObject asObject() { throw new IllegalStateException("Not a JSON object"); }
    /**
     * {@return this as an array}
     *
     * @throws IllegalStateException if it isn't one
     */
    default JsonArray asArray() { throw new IllegalStateException("Not a JSON array"); }
    /**
     * {@return this as a string}
     *
     * @throws IllegalStateException if it isn't one
     */
    default String asString() { throw new IllegalStateException("Not a JSON string"); }
    /**
     * {@return this as a number}
     *
     * @throws IllegalStateException if it isn't one
     */
    default java.math.BigDecimal asNumber() { throw new IllegalStateException("Not a JSON number"); }
    /**
     * {@return this as an int}
     *
     * @throws IllegalStateException if it isn't a number
     */
    default int asInt() { throw new IllegalStateException("Not a JSON int"); }
    /**
     * {@return this as a boolean}
     *
     * @throws IllegalStateException if it isn't one
     */
    default boolean asBoolean() { throw new IllegalStateException("Not a JSON boolean"); }

    /**
     * {@return this as JSON text}
     */
    String toJson();
}

