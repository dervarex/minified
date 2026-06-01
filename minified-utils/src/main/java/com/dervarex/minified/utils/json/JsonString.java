package com.dervarex.minified.utils.json;

public final class JsonString implements JsonValue {
    private final String value;

    public JsonString(String value) {
        this.value = value == null ? "" : value;
    }

    public String getValue() { return value; }

    @Override
    public JsonType getType() { return JsonType.STRING; }

    @Override
    public String asString() { return value; }

    @Override
    public String toJson() { return "\"" + JsonWriter.escape(value) + "\""; }

    @Override
    public String toString() { return value; }
}

