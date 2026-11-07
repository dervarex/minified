/**
 * JSON model, parsing, and writing utilities.
 * Made specifically for easy json reading, for more advanced json parsing or writing, please use another utility such as GSON.
 * <br>
 * A document gets read into memory as a tree of {@link com.dervarex.minified.utils.json.JsonValue}s,
 * {@code toJson()} writes it back out compact, without pretty printing
 *
 * <h2>Parsing</h2>
 * <pre>{@code
 * JsonValue root = JsonParser.parse(jsonString);
 * }</pre>
 * For files, {@link com.dervarex.minified.utils.json.JsonFile} reads and parses:
 * <pre>{@code
 * JsonFile config = new JsonFile(Path.of("config.json"));
 * String name = config.getString("launcherName");
 * }</pre>
 * Invalid JSON throws a {@link com.dervarex.minified.utils.json.JsonParseException} with the position where it blew up
 *
 * <h2>Types</h2>
 * <ul>
 *     <li>{@link com.dervarex.minified.utils.json.JsonObject}: keys with values</li>
 *     <li>{@link com.dervarex.minified.utils.json.JsonArray}: a list of values, works in a for-each loop</li>
 *     <li>{@link com.dervarex.minified.utils.json.JsonString}: a string, null becomes ""</li>
 *     <li>{@link com.dervarex.minified.utils.json.JsonNumber}: backed by a {@link java.math.BigDecimal}, so large numbers stay exact</li>
 *     <li>{@link com.dervarex.minified.utils.json.JsonBoolean}: a boolean</li>
 *     <li>{@link com.dervarex.minified.utils.json.JsonNull}: JSON null</li>
 * </ul>
 * The {@code asObject()}, {@code asString()} and other {@code as} methods throw if the value is another type.
 * Check with {@code isString()} and other {@code isXyz()} first, or use the getters below
 *
 * <h2>Reading values</h2>
 * The getters on {@link com.dervarex.minified.utils.json.JsonObject}, {@link com.dervarex.minified.utils.json.JsonArray}
 * and {@link com.dervarex.minified.utils.json.JsonFile} return null if the value is null or the key is missing, and throw
 * if it's another type
 * <pre>{@code
 * JsonObject javaVersion = versionJson.getObject("javaVersion");
 * Integer majorVersion = javaVersion != null ? javaVersion.getInt("majorVersion") : null;
 * }</pre>
 *
 * <h2>Writing</h2>
 * {@code put} and {@code add} take strings, numbers and booleans directly:
 * <pre>{@code
 * JsonObject session = new JsonObject();
 * session.put("username", "Player");
 * session.put("expiresAt", System.currentTimeMillis());
 * session.put("valid", true);
 *
 * String json = session.toJson();
 * }</pre>
 * {@link com.dervarex.minified.utils.json.JsonFile#save()} only works for a file that was read from a path, use
 * {@link com.dervarex.minified.utils.json.JsonFile#save(java.nio.file.Path)} for everything else:
 * <pre>{@code
 * JsonFile file = new JsonFile();
 * file.asObject().put("launcherName", "MyLauncher");
 * file.save(Path.of("config.json"));
 * }</pre>
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.utils.json;

import org.apiguardian.api.API;
