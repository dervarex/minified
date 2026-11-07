package com.dervarex.minified.utils.json;

/**
 * Thrown when JSON text isn't valid
 */
public class JsonParseException extends RuntimeException {
    private final int position;

    /**
     * @param message what went wrong
     * @param position where in the text it went wrong
     */
    public JsonParseException(String message, int position) {
        super(message + " at position " + position);
        this.position = position;
    }

    /**
     * {@return the position}
     */
    public int getPosition() { return position; }
}

