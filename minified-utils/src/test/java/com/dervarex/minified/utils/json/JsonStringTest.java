package com.dervarex.minified.utils.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonStringTest {

    @Test
    void everythingThatGetsWrittenCanBeReadBack() {
        String ugly = "quote\" backslash\\ tab\t newline\n bell\u0007 é";

        assertEquals(ugly, JsonParser.parse(new JsonString(ugly).toJson()).asString());
    }
}
