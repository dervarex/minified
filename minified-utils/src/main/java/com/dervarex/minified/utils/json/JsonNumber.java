package com.dervarex.minified.utils.json;

import java.math.BigDecimal;

public final class JsonNumber implements JsonValue {
    private final BigDecimal value;

    public JsonNumber(BigDecimal value) {
        this.value = value == null ? BigDecimal.ZERO : value;
    }

    public BigDecimal getValue() { return value; }

    @Override
    public JsonType getType() { return JsonType.NUMBER; }

    @Override
    public BigDecimal asNumber() { return value; }

    @Override
    public String toJson() { return value.toPlainString(); }

    @Override
    public String toString() { return value.toPlainString(); }
}

