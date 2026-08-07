package com.dervarex.minified.utils.json;

public record JsonBoolean(boolean value) implements JsonValue {

    @Override
    public JsonType getType() {
        return JsonType.BOOLEAN;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    public String toJson() {
        return value ? "true" : "false";
    }

    @Override
    public String toString() {
        return toJson();
    }
}

