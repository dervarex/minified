package com.dervarex.minified.utils.json;

import java.math.BigDecimal;

/**
 * A JSON number
 *
 * @param value the value
 */
public record JsonNumber(BigDecimal value) implements JsonValue {
    /**
     * @param value the value, null becomes 0
     */
    public JsonNumber(BigDecimal value) {
        this.value = value == null ? BigDecimal.ZERO : value;
    }

    /**
     * {@return {@link JsonType#NUMBER}}
     */
    @Override
    public JsonType getType() {
        return JsonType.NUMBER;
    }

    /**
     * {@return the value}
     */
    @Override
    public BigDecimal asNumber() {
        return value;
    }

    /**
     * {@return the value as an int}
     */
    @Override
    public int asInt() {
        return value.intValue();
    }

    /**
     * {@return the value without an exponent}
     */
    @Override
    public String toJson() {
        return value.toPlainString();
    }

    /**
     * {@return same as {@link #toJson()}}
     */
    @Override
    public String toString() {
        return value.toPlainString();
    }
}

