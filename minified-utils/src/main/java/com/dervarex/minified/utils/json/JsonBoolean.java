package com.dervarex.minified.utils.json;

public final class JsonBoolean implements JsonValue {
    private final boolean value;

    public JsonBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() { return value; }

    @Override
    public JsonType getType() { return JsonType.BOOLEAN; }

    @Override
    public boolean asBoolean() { return value; }

    @Override
    public String toJson() { return value ? "true" : "false"; }

    @Override
    public String toString() { return toJson(); }
}

