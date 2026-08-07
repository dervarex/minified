package com.dervarex.minified.utils.json;

import java.math.BigDecimal;

public record JsonNumber(BigDecimal value) implements JsonValue {
    public JsonNumber(BigDecimal value) {
        this.value = value == null ? BigDecimal.ZERO : value;
    }

    @Override
    public JsonType getType() {
        return JsonType.NUMBER;
    }

    @Override
    public BigDecimal asNumber() {
        return value;
    }

    @Override
    public int asInt() {
        return value.intValue();
    }

    @Override
    public String toJson() {
        return value.toPlainString();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}

