package com.dervarex.minified.utils.json;

public record JsonString(String value) implements JsonValue {
    public JsonString(String value) {
        this.value = value == null ? "" : value;
    }

    @Override
    public JsonType getType() {
        return JsonType.STRING;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public String toJson() {
        return "\"" + JsonWriter.escape(value) + "\"";
    }

    @Override
    public String toString() {
        return value;
    }
}

