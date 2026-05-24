package com.dervarex.minified.utils.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser {
    private final String input;
    private int index;

    public JsonParser(String input) {
        this.input = input == null ? "" : input;
        this.index = 0;
    }

    public static JsonValue parse(String input) {
        return new JsonParser(input).parseRoot();
    }

    private JsonValue parseRoot() {
        skipWhitespace();
        JsonValue value = parseValue();
        skipWhitespace();
        if (index != input.length()) {
            throw new JsonParseException("Unexpected trailing data", index);
        }
        return value;
    }

    private JsonValue parseValue() {
        skipWhitespace();
        if (index >= input.length()) {
            throw new JsonParseException("Unexpected end of input", index);
        }
        char c = input.charAt(index);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return new JsonString(parseString());
        if (c == 't') return parseLiteral("true", new JsonBoolean(true));
        if (c == 'f') return parseLiteral("false", new JsonBoolean(false));
        if (c == 'n') return parseLiteral("null", JsonNull.INSTANCE);
        if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();
        throw new JsonParseException("Invalid JSON value", index);
    }

    private JsonValue parseLiteral(String literal, JsonValue value) {
        if (input.startsWith(literal, index)) {
            index += literal.length();
            return value;
        }
        throw new JsonParseException("Invalid literal", index);
    }

    private JsonObject parseObject() {
        expect('{');
        skipWhitespace();
        Map<String, JsonValue> values = new LinkedHashMap<>();
        if (peek('}')) {
            index++;
            return new JsonObject(values);
        }
        while (true) {
            skipWhitespace();
            if (!peek('"')) throw new JsonParseException("Expected string key", index);
            String key = parseString();
            skipWhitespace();
            expect(':');
            JsonValue value = parseValue();
            values.put(key, value);
            skipWhitespace();
            if (peek('}')) {
                index++;
                break;
            }
            expect(',');
        }
        return new JsonObject(values);
    }

    private JsonArray parseArray() {
        expect('[');
        skipWhitespace();
        List<JsonValue> values = new ArrayList<>();
        if (peek(']')) {
            index++;
            return new JsonArray(values);
        }
        while (true) {
            JsonValue value = parseValue();
            values.add(value);
            skipWhitespace();
            if (peek(']')) {
                index++;
                break;
            }
            expect(',');
        }
        return new JsonArray(values);
    }

    private JsonValue parseNumber() {
        int start = index;
        if (peek('-')) index++;
        if (peek('0')) {
            index++;
        } else {
            readDigits();
        }
        if (peek('.')) {
            index++;
            readDigits();
        }
        if (peek('e') || peek('E')) {
            index++;
            if (peek('+') || peek('-')) index++;
            readDigits();
        }
        String raw = input.substring(start, index);
        try {
            return new JsonNumber(new BigDecimal(raw));
        } catch (NumberFormatException e) {
            throw new JsonParseException("Invalid number", start);
        }
    }

    private void readDigits() {
        int start = index;
        while (index < input.length()) {
            char c = input.charAt(index);
            if (c < '0' || c > '9') break;
            index++;
        }
        if (index == start) {
            throw new JsonParseException("Expected digit", index);
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (index < input.length()) {
            char c = input.charAt(index++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (index >= input.length()) throw new JsonParseException("Unterminated escape", index);
                char esc = input.charAt(index++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        sb.append(parseUnicode());
                        break;
                    default:
                        throw new JsonParseException("Invalid escape", index - 1);
                }
            } else {
                if (c < 0x20) throw new JsonParseException("Invalid control character", index - 1);
                sb.append(c);
            }
        }
        throw new JsonParseException("Unterminated string", index);
    }

    private char parseUnicode() {
        if (index + 4 > input.length()) throw new JsonParseException("Invalid unicode escape", index);
        int codePoint = 0;
        for (int i = 0; i < 4; i++) {
            char c = input.charAt(index++);
            int value = Character.digit(c, 16);
            if (value == -1) throw new JsonParseException("Invalid unicode escape", index - 1);
            codePoint = (codePoint << 4) + value;
        }
        return (char) codePoint;
    }

    private void skipWhitespace() {
        while (index < input.length()) {
            char c = input.charAt(index);
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t') break;
            index++;
        }
    }

    private boolean peek(char c) {
        return index < input.length() && input.charAt(index) == c;
    }

    private void expect(char c) {
        if (!peek(c)) throw new JsonParseException("Expected '" + c + "'", index);
        index++;
    }
}

