package com.dervarex.minified.utils.json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A JSON document, read from a file or a string
 */
public final class JsonFile {
    private final Path path;
    private JsonValue root;

    /**
     * Reads and parses a file
     *
     * @param file the file
     * @throws IOException if the file can't be read
     */
    public JsonFile(File file) throws IOException {
        this(file.toPath());
    }
    /**
     * An empty object without a path
     */
    public JsonFile() {
        this.path = null;
        this.root = new JsonObject();
    }
    /**
     * Writes it back to the file it was read from
     *
     * @throws IllegalStateException if there's no path
     * @throws IOException if the file can't be written
     */
    public void save() throws IOException {
        if (path == null) {
            throw new IllegalStateException("No path associated with this JsonFile");
        }

        Files.writeString(path, root.toJson(), StandardCharsets.UTF_8);
    }

    /**
     * Writes it to a file
     *
     * @param path the file
     * @throws IOException if the file can't be written
     */
    public void save(Path path) throws IOException {
        Files.writeString(path, root.toJson(), StandardCharsets.UTF_8);
    }

    /**
     * Reads and parses a file
     *
     * @param path the file
     * @throws IOException if the file can't be read
     */
    public JsonFile(Path path) throws IOException {
        this.path = path;
        String content = Files.readString(path, StandardCharsets.UTF_8);
        this.root = JsonParser.parse(content);
    }

    /**
     * Parses JSON text, without a path
     *
     * @param content the JSON text, null counts as empty
     */
    public JsonFile(String content) {
        this.path = null;
        String payload = content == null ? "" : content;
        this.root = JsonParser.parse(payload);
    }

    /**
     * {@return the root}
     */
    public JsonValue getRoot() { return root; }

    /**
     * {@return the path}
     */
    public Path getPath() { return path; }

    /**
     * {@return true if the root is an object}
     */
    public boolean isObject() { return root.isObject(); }

    /**
     * {@return true if the root is an array}
     */
    public boolean isArray() { return root.isArray(); }

    /**
     * {@return the root as an object}
     *
     * @throws IllegalStateException if it isn't one
     */
    public JsonObject asObject() { return root.asObject(); }

    /**
     * {@return the root as an array}
     *
     * @throws IllegalStateException if it isn't one
     */
    public JsonArray asArray() { return root.asArray(); }

    /**
     * {@return a value of the root object, null if it's missing}
     *
     * @param key the key
     */
    public JsonValue get(String key) { return asObject().get(key); }

    /**
     * {@return a string of the root object, null if it's missing or null}
     *
     * @param key the key
     */
    public String getString(String key) { return asObject().getString(key); }

    /**
     * {@return a number of the root object, null if it's missing or null}
     *
     * @param key the key
     */
    public java.math.BigDecimal getNumber(String key) { return asObject().getNumber(key); }

    /**
     * {@return a boolean of the root object, null if it's missing or null}
     *
     * @param key the key
     */
    public Boolean getBoolean(String key) { return asObject().getBoolean(key); }

    /**
     * {@return an object of the root object, null if it's missing or null}
     *
     * @param key the key
     */
    public JsonObject getObject(String key) { return asObject().getObject(key); }

    /**
     * {@return an array of the root object, null if it's missing or null}
     *
     * @param key the key
     */
    public JsonArray getArray(String key) { return asObject().getArray(key); }

    /**
     * {@return the root as JSON text}
     */
    public String toJson() { return root.toJson(); }
}
