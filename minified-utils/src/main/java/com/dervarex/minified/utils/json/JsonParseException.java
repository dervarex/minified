package com.dervarex.minified.utils.json;

public class JsonParseException extends RuntimeException {
    private final int position;

    public JsonParseException(String message, int position) {
        super(message + " at position " + position);
        this.position = position;
    }

    /**
     * {@return the position}
     */
    public int getPosition() { return position; }
}

