package com.dervarex.minified.utils.json;

public final class JsonNull implements JsonValue {
    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    @Override
    public JsonType getType() { return JsonType.NULL; }

    @Override
    public String toJson() { return "null"; }

    @Override
    public String toString() { return toJson(); }
}

