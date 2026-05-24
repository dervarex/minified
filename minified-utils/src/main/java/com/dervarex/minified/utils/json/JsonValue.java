package com.dervarex.minified.utils.json;

public interface JsonValue {
    JsonType getType();

    default boolean isObject() { return getType() == JsonType.OBJECT; }
    default boolean isArray() { return getType() == JsonType.ARRAY; }
    default boolean isString() { return getType() == JsonType.STRING; }
    default boolean isNumber() { return getType() == JsonType.NUMBER; }
    default boolean isBoolean() { return getType() == JsonType.BOOLEAN; }
    default boolean isNull() { return getType() == JsonType.NULL; }

    default JsonObject asObject() { throw new IllegalStateException("Not a JSON object"); }
    default JsonArray asArray() { throw new IllegalStateException("Not a JSON array"); }
    default String asString() { throw new IllegalStateException("Not a JSON string"); }
    default java.math.BigDecimal asNumber() { throw new IllegalStateException("Not a JSON number"); }
    default int asInt() { throw new IllegalStateException("Not a JSON int"); }
    default boolean asBoolean() { throw new IllegalStateException("Not a JSON boolean"); }

    String toJson();
}

