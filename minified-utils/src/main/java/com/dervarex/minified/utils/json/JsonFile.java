package com.dervarex.minified.utils.json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class JsonFile {
    private final Path path;
    private JsonValue root;

    public JsonFile(File file) throws IOException {
        this(file.toPath());
    }
    public JsonFile() {
        this.path = null;
        this.root = new JsonObject();
    }
    public void save() throws IOException {
        if (path == null) {
            throw new IllegalStateException("No path associated with this JsonFile");
        }

        Files.writeString(path, root.toJson(), StandardCharsets.UTF_8);
    }

    public void save(Path path) throws IOException {
        Files.writeString(path, root.toJson(), StandardCharsets.UTF_8);
    }

    public JsonFile(Path path) throws IOException {
        this.path = path;
        String content = Files.readString(path, StandardCharsets.UTF_8);
        this.root = JsonParser.parse(content);
    }

    public JsonFile(String content) {
        this.path = null;
        String payload = content == null ? "" : content;
        this.root = JsonParser.parse(payload);
    }

    public JsonValue getRoot() { return root; }

    public Path getPath() { return path; }

    public boolean isObject() { return root.isObject(); }

    public boolean isArray() { return root.isArray(); }

    public JsonObject asObject() { return root.asObject(); }

    public JsonArray asArray() { return root.asArray(); }

    public JsonValue get(String key) { return asObject().get(key); }

    public String getString(String key) { return asObject().getString(key); }

    public java.math.BigDecimal getNumber(String key) { return asObject().getNumber(key); }

    public Boolean getBoolean(String key) { return asObject().getBoolean(key); }

    public JsonObject getObject(String key) { return asObject().getObject(key); }

    public JsonArray getArray(String key) { return asObject().getArray(key); }

    public String toJson() { return root.toJson(); }
}
