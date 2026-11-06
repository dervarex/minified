## http.HttpResponse

`minified-utils/src/main/java/com/dervarex/minified/utils/http/HttpResponse.java`

- [ ] L10 `HttpResponse(int,String,Map,byte[])`: @param statusCode, @param statusMessage, @param headers, @param body  
  `public record HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body)`
- [ ] L10 `HttpResponse`: @param statusCode (record component), @param statusMessage (record component), @param headers (record component), @param body (record component), no description  
  `public record HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body)`
- [ ] L23 `getBodyAsString()`: @return  
  `public String getBodyAsString()`

## http.HttpUtil

`minified-utils/src/main/java/com/dervarex/minified/utils/http/HttpUtil.java`

- [ ] L25 `get(String)`: no description, @param url, @return, @throws ?  
  `public static String get(String url) throws IOException, HttpException`
- [ ] L25 `get(String,int)`: no description, @param url, @param timeoutMs, @return, @throws ?  
  `public static String get(String url) throws IOException, HttpException`
- [ ] L30 `request(String,String,Map,byte[],int,int)`: no description, @param method, @param url, @param headers, @param body, @param connectTimeoutMs, @param readTimeoutMs, @return, @throws ?  
  `HttpResponse response = request("GET", url, Map.of(), null, timeoutMs, timeoutMs);`
- [ ] L70 `requestJson(String,String,String)`: no description, @param method, @param url, @param jsonBody, @return, @throws ?  
  `public static HttpResponse requestJson(String method, String url, String jsonBody) throws IOException, HttpException`

## json.JsonArray

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonArray.java`

- [ ] L9 `JsonArray`: @param values (record component), no description  
  `public record JsonArray(List<JsonValue> values) implements JsonValue, Iterable<JsonValue>`
- [ ] L14 `JsonArray(List)`: @param values  
  `public JsonArray(List<JsonValue> values)`
- [ ] L23 `add(JsonValue)`: no description, @param value  
  `public void add(JsonValue value)`
- [ ] L23 `add(Number)`: no description, @param value  
  `public void add(JsonValue value)`
- [ ] L23 `add(String)`: no description, @param value  
  `public void add(JsonValue value)`
- [ ] L23 `add(boolean)`: no description, @param value  
  `public void add(JsonValue value)`
- [ ] L27 `size()`: no description, @return  
  `public int size()`
- [ ] L31 `get(int)`: no description, @param index, @return  
  `public JsonValue get(int index)`
- [ ] L35 `getObject(int)`: no description, @param index, @return  
  `public JsonObject getObject(int index)`
- [ ] L40 `getArray(int)`: no description, @param index, @return  
  `public JsonArray getArray(int index)`
- [ ] L42 `asArray()`: no description, @return  
  `return value == null || value.isNull() ? null : value.asArray();`
- [ ] L45 `getString(int)`: no description, @param index, @return  
  `public String getString(int index)`
- [ ] L50 `getBoolean(int)`: no description, @param index, @return  
  `public Boolean getBoolean(int index)`
- [ ] L55 `getNumber(int)`: no description, @param index, @return  
  `public BigDecimal getNumber(int index)`
- [ ] L61 `getType()`: no description, @return  
  `public JsonType getType()`
- [ ] L71 `toJson()`: no description, @return  
  `public String toJson()`
- [ ] L80 `toString()`: no description, @return  
  `return sb.toString();`
- [ ] L89 `iterator()`: no description, @return  
  `public Iterator<JsonValue> iterator()`
- [ ] L105 `addNull()`: no description  
  `public void addNull()`
- [ ] L109 `remove(int)`: no description, @param index, @return  
  `public JsonValue remove(int index)`
- [ ] L113 `clear()`: no description  
  `public void clear()`

## json.JsonBoolean

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonBoolean.java`

- [ ] L3 `JsonBoolean(boolean)`: @param value  
  `public record JsonBoolean(boolean value) implements JsonValue`
- [ ] L3 `JsonBoolean`: @param value (record component), no description  
  `public record JsonBoolean(boolean value) implements JsonValue`
- [ ] L6 `getType()`: no description, @return  
  `public JsonType getType()`
- [ ] L11 `asBoolean()`: no description, @return  
  `public boolean asBoolean()`
- [ ] L16 `toJson()`: no description, @return  
  `public String toJson()`
- [ ] L21 `toString()`: no description, @return  
  `public String toString()`

## json.JsonFile

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonFile.java`

- [ ] L9 `JsonFile`: no description  
  `public final class JsonFile`
- [ ] L13 `JsonFile(File)`: @param file, @throws ?  
  `public JsonFile(File file) throws IOException`
- [ ] L13 `JsonFile(Path)`: @param path, @throws ?  
  `public JsonFile(File file) throws IOException`
- [ ] L13 `JsonFile(String)`: @param content  
  `public JsonFile(File file) throws IOException`
- [ ] L20 `save()`: no description, @throws ?  
  `public void save() throws IOException`
- [ ] L20 `save(Path)`: no description, @param path, @throws ?  
  `public void save() throws IOException`
- [ ] L25 `toJson()`: no description, @return  
  `Files.writeString(path, root.toJson(), StandardCharsets.UTF_8);`
- [ ] L54 `isObject()`: no description, @return  
  `public boolean isObject() { return root.isObject(); }`
- [ ] L56 `isArray()`: no description, @return  
  `public boolean isArray() { return root.isArray(); }`
- [ ] L58 `asObject()`: no description, @return  
  `public JsonObject asObject() { return root.asObject(); }`
- [ ] L60 `asArray()`: no description, @return  
  `public JsonArray asArray() { return root.asArray(); }`
- [ ] L62 `get(String)`: no description, @param key, @return  
  `public JsonValue get(String key) { return asObject().get(key); }`
- [ ] L64 `getString(String)`: no description, @param key, @return  
  `public String getString(String key) { return asObject().getString(key); }`
- [ ] L66 `getNumber(String)`: no description, @param key, @return  
  `public java.math.BigDecimal getNumber(String key) { return asObject().getNumber(key); }`
- [ ] L68 `getBoolean(String)`: no description, @param key, @return  
  `public Boolean getBoolean(String key) { return asObject().getBoolean(key); }`
- [ ] L70 `getObject(String)`: no description, @param key, @return  
  `public JsonObject getObject(String key) { return asObject().getObject(key); }`
- [ ] L72 `getArray(String)`: no description, @param key, @return  
  `public JsonArray getArray(String key) { return asObject().getArray(key); }`

## json.JsonNull

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonNull.java`

- [ ] L3 `JsonNull`: no description  
  `public final class JsonNull implements JsonValue`
- [ ] L9 `getType()`: no description, @return  
  `public JsonType getType() { return JsonType.NULL; }`
- [ ] L12 `toJson()`: no description, @return  
  `public String toJson() { return "null"; }`
- [ ] L15 `toString()`: no description, @return  
  `public String toString() { return toJson(); }`

## json.JsonNumber

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonNumber.java`

- [ ] L5 `JsonNumber`: @param value (record component), no description  
  `public record JsonNumber(BigDecimal value) implements JsonValue`
- [ ] L6 `JsonNumber(BigDecimal)`: @param value  
  `public JsonNumber(BigDecimal value)`
- [ ] L11 `getType()`: no description, @return  
  `public JsonType getType()`
- [ ] L16 `asNumber()`: no description, @return  
  `public BigDecimal asNumber()`
- [ ] L21 `asInt()`: no description, @return  
  `public int asInt()`
- [ ] L26 `toJson()`: no description, @return  
  `public String toJson()`
- [ ] L31 `toString()`: no description, @return  
  `public String toString()`

## json.JsonObject

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonObject.java`

- [ ] L9 `JsonObject`: no description  
  `public final class JsonObject implements JsonValue`
- [ ] L12 `JsonObject(Map)`: @param values  
  `public JsonObject(Map<String, JsonValue> values)`
- [ ] L18 `put(String,JsonValue)`: no description, @param key, @param value  
  `public void put(String key, JsonValue value)`
- [ ] L18 `put(String,Number)`: no description, @param key, @param value  
  `public void put(String key, JsonValue value)`
- [ ] L18 `put(String,String)`: no description, @param key, @param value  
  `public void put(String key, JsonValue value)`
- [ ] L18 `put(String,boolean)`: no description, @param key, @param value  
  `public void put(String key, JsonValue value)`
- [ ] L27 `toString()`: no description, @return  
  `put(key, new JsonNumber(new BigDecimal(value.toString())));`
- [ ] L34 `putNull(String)`: no description, @param key  
  `public void putNull(String key)`
- [ ] L38 `remove(String)`: no description, @param key, @return  
  `public JsonValue remove(String key)`
- [ ] L42 `clear()`: no description  
  `public void clear()`
- [ ] L46 `size()`: no description, @return  
  `public int size()`
- [ ] L50 `keys()`: no description, @return  
  `public Set<String> keys() { return Collections.unmodifiableSet(values.keySet()); }`
- [ ] L51 `containsKey(String)`: no description, @param key, @return  
  `public boolean containsKey(String key) { return values.containsKey(key); }`
- [ ] L53 `entries()`: no description, @return  
  `public Map<String, JsonValue> entries() { return Collections.unmodifiableMap(values); }`
- [ ] L55 `get(String)`: no description, @param key, @return  
  `public JsonValue get(String key) { return values.get(key); }`
- [ ] L57 `has(String)`: no description, @param key, @return  
  `public boolean has(String key) { return values.containsKey(key); }`
- [ ] L59 `getObject(String)`: no description, @param key, @return  
  `public JsonObject getObject(String key)`
- [ ] L61 `asObject()`: no description, @return  
  `return value == null || value.isNull() ? null : value.asObject();`
- [ ] L64 `getArray(String)`: no description, @param key, @return  
  `public JsonArray getArray(String key)`
- [ ] L69 `getString(String)`: no description, @param key, @return  
  `public String getString(String key)`
- [ ] L74 `getNumber(String)`: no description, @param key, @return  
  `public java.math.BigDecimal getNumber(String key)`
- [ ] L79 `getBoolean(String)`: no description, @param key, @return  
  `public Boolean getBoolean(String key)`
- [ ] L84 `getInt(String)`: no description, @param key, @return  
  `public Integer getInt(String key)`
- [ ] L89 `getLong(String)`: no description, @param key, @return  
  `public Long getLong(String key)`
- [ ] L94 `getDouble(String)`: no description, @param key, @return  
  `public Double getDouble(String key)`
- [ ] L100 `getType()`: no description, @return  
  `public JsonType getType() { return JsonType.OBJECT; }`
- [ ] L106 `toJson()`: no description, @return  
  `public String toJson()`
- [ ] L122 `equals(Object)`: no description, @param o, @return  
  `public boolean equals(Object o)`
- [ ] L127 `hashCode()`: no description, @return  
  `public int hashCode() { return values.hashCode(); }`

## json.JsonParseException

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonParseException.java`

- [ ] L3 `JsonParseException`: no description  
  `public class JsonParseException extends RuntimeException`
- [ ] L6 `JsonParseException(String,int)`: @param message, @param position  
  `public JsonParseException(String message, int position)`

## json.JsonParser

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonParser.java`

- [ ] L9 `JsonParser`: no description  
  `public final class JsonParser`
- [ ] L13 `JsonParser(String)`: @param input  
  `public JsonParser(String input)`
- [ ] L18 `parse(String)`: @param input, @return  
  `public static JsonValue parse(String input)`

## json.JsonString

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonString.java`

- [ ] L3 `JsonString`: @param value (record component), no description  
  `public record JsonString(String value) implements JsonValue`
- [ ] L4 `JsonString(String)`: @param value  
  `public JsonString(String value)`
- [ ] L9 `getType()`: no description, @return  
  `public JsonType getType()`
- [ ] L14 `asString()`: no description, @return  
  `public String asString()`
- [ ] L19 `toJson()`: no description, @return  
  `public String toJson()`
- [ ] L24 `toString()`: no description, @return  
  `public String toString()`

## json.JsonType

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonType.java`

- [ ] L3 `JsonType`: no description  
  `public enum JsonType`

## json.JsonValue

`minified-utils/src/main/java/com/dervarex/minified/utils/json/JsonValue.java`

- [ ] L3 `JsonValue`: no description  
  `public interface JsonValue`
- [ ] L4 `getType()`: no description, @return  
  `JsonType getType();`
- [ ] L6 `isObject()`: no description, @return  
  `default boolean isObject() { return getType() == JsonType.OBJECT; }`
- [ ] L7 `isArray()`: no description, @return  
  `default boolean isArray() { return getType() == JsonType.ARRAY; }`
- [ ] L8 `isString()`: no description, @return  
  `default boolean isString() { return getType() == JsonType.STRING; }`
- [ ] L9 `isNumber()`: no description, @return  
  `default boolean isNumber() { return getType() == JsonType.NUMBER; }`
- [ ] L10 `isBoolean()`: no description, @return  
  `default boolean isBoolean() { return getType() == JsonType.BOOLEAN; }`
- [ ] L11 `isNull()`: no description, @return  
  `default boolean isNull() { return getType() == JsonType.NULL; }`
- [ ] L13 `asObject()`: no description, @return  
  `default JsonObject asObject() { throw new IllegalStateException("Not a JSON object"); }`
- [ ] L14 `asArray()`: no description, @return  
  `default JsonArray asArray() { throw new IllegalStateException("Not a JSON array"); }`
- [ ] L15 `asString()`: no description, @return  
  `default String asString() { throw new IllegalStateException("Not a JSON string"); }`
- [ ] L16 `asNumber()`: no description, @return  
  `default java.math.BigDecimal asNumber() { throw new IllegalStateException("Not a JSON number"); }`
- [ ] L17 `asInt()`: no description, @return  
  `default int asInt() { throw new IllegalStateException("Not a JSON int"); }`
- [ ] L18 `asBoolean()`: no description, @return  
  `default boolean asBoolean() { throw new IllegalStateException("Not a JSON boolean"); }`
- [ ] L20 `toJson()`: no description, @return  
  `String toJson();`

## nbt.NbtEquals

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/NbtEquals.java`

- [ ] L16 `deepEquals(NbtTag,NbtTag)`: @param a, @param b, @return  
  `public static boolean deepEquals(NbtTag a, NbtTag b)`

## nbt.NbtWriter

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/NbtWriter.java`

- [ ] L10 `NbtWriter`: no description  
  `public class NbtWriter`
- [ ] L15 `writeCompound(DataOutputStream,NbtCompound)`: @param out, @param compound, @throws ?  
  `public static void writeCompound(DataOutputStream out, NbtCompound compound) throws IOException`

## nbt.Parser

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/Parser.java`

- [ ] L10 `Parser`: no description  
  `public class Parser`
- [ ] L21 `readFile(File)`: @return, @throws ?  
  `public static NbtCompound readFile(File file) throws IOException`

## nbt.RegionFile

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/RegionFile.java`

- [ ] L34 `open(File)`: no description, @param file, @return, @throws ?  
  `public static RegionFile open(File file) throws IOException`
- [ ] L46 `create(File)`: @param file, @return, @throws ?  
  `public static RegionFile create(File file) throws IOException`
- [ ] L80 `hasChunk(int,int)`: no description, @param chunkX, @param chunkZ, @return  
  `public boolean hasChunk(int chunkX, int chunkZ)`
- [ ] L85 `readChunk(int,int)`: no description, @param chunkX, @param chunkZ, @return, @throws ?  
  `public NbtCompound readChunk(int chunkX, int chunkZ) throws IOException`
- [ ] L135 `writeChunk(int,int,NbtCompound)`: @param chunkX, @param chunkZ, @param chunkData  
  `public void writeChunk(int chunkX, int chunkZ, NbtCompound chunkData) throws IOException`
- [ ] L201 `close()`: no description, @throws ?  
  `public void close() throws IOException`

## nbt.Writer

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/Writer.java`

- [ ] L18 `Writer`: no description  
  `public class Writer`
- [ ] L27 `writeFile(File,NbtCompound,File)`: @param backup  
  `public static void writeFile(File file, NbtCompound nbt) throws IOException`
- [ ] L49 `writeFileUncompressed(File,NbtCompound)`: @throws ?  
  `public static void writeFileUncompressed(File file, NbtCompound nbt) throws IOException`

## nbt.tag.NbtBoolean

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtBoolean.java`

- [ ] L6 `NbtBoolean(boolean)`: @param value  
  `public record NbtBoolean(boolean value) implements NbtTag`
- [ ] L6 `NbtBoolean`: @param value (record component), no description  
  `public record NbtBoolean(boolean value) implements NbtTag`
- [ ] L7 `id()`: @return  
  `public byte id() { return 1; }`

## nbt.tag.NbtByte

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtByte.java`

- [ ] L6 `NbtByte(byte)`: @param value  
  `public record NbtByte(byte value) implements NbtTag`
- [ ] L6 `NbtByte`: @param value (record component), no description  
  `public record NbtByte(byte value) implements NbtTag`
- [ ] L7 `id()`: @return  
  `public byte id() { return 1; }`

## nbt.tag.NbtByteArray

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtByteArray.java`

- [ ] L6 `NbtByteArray(byte[])`: @param value  
  `public record NbtByteArray(byte[] value) implements NbtTag`
- [ ] L6 `NbtByteArray`: @param value (record component), no description  
  `public record NbtByteArray(byte[] value) implements NbtTag`
- [ ] L7 `id()`: no description, @return  
  `public byte id() { return 7; }`
- [ ] L9 `copy()`: @return  
  `public NbtByteArray copy() { return new NbtByteArray(value.clone()); }`

## nbt.tag.NbtCompound

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtCompound.java`

- [ ] L12 `NbtCompound`: no description  
  `public final class NbtCompound implements NbtTag`
- [ ] L15 `id()`: no description, @return  
  `public byte id() { return 10; }`
- [ ] L17 `put(String,NbtTag)`: no description, @param key, @param value  
  `public void put(String key, NbtTag value)`
- [ ] L22 `setString(String,String)`: no description, @param key, @param value  
  `public void setString(String key, String value) { put(key, new NbtString(value)); }`
- [ ] L23 `setInt(String,int)`: no description, @param key, @param value  
  `public void setInt(String key, int value)         { put(key, new NbtInt(value)); }`
- [ ] L24 `setLong(String,long)`: no description, @param key, @param value  
  `public void setLong(String key, long value)        { put(key, new NbtLong(value)); }`
- [ ] L25 `setShort(String,short)`: no description, @param key, @param value  
  `public void setShort(String key, short value)      { put(key, new NbtShort(value)); }`
- [ ] L26 `setByte(String,byte)`: no description, @param key, @param value  
  `public void setByte(String key, byte value)        { put(key, new NbtByte(value)); }`
- [ ] L27 `setBoolean(String,boolean)`: no description, @param key, @param value  
  `public void setBoolean(String key, boolean value)  { put(key, new NbtBoolean(value)); }`
- [ ] L28 `setDouble(String,double)`: no description, @param key, @param value  
  `public void setDouble(String key, double value)     { put(key, new NbtDouble(value)); }`
- [ ] L29 `setFloat(String,float)`: no description, @param key, @param value  
  `public void setFloat(String key, float value)        { put(key, new NbtFloat(value)); }`
- [ ] L30 `setByteArray(String,byte[])`: no description, @param key, @param value  
  `public void setByteArray(String key, byte[] value)  { put(key, new NbtByteArray(value)); }`
- [ ] L31 `setIntArray(String,int[])`: no description, @param key, @param value  
  `public void setIntArray(String key, int[] value)    { put(key, new NbtIntArray(value)); }`
- [ ] L32 `setLongArray(String,long[])`: no description, @param key, @param value  
  `public void setLongArray(String key, long[] value)  { put(key, new NbtLongArray(value)); }`
- [ ] L33 `setCompound(String,NbtCompound)`: no description, @param key, @param value  
  `public void setCompound(String key, NbtCompound value) { put(key, value); }`
- [ ] L34 `setList(String,NbtList)`: no description, @param key, @param value  
  `public void setList(String key, NbtList value) { put(key, value); }`
- [ ] L36 `get(String)`: no description, @param key, @return  
  `public Optional<NbtTag> get(String key)`
- [ ] L40 `getBoolean(String)`: no description, @param key, @return  
  `public boolean getBoolean(String key)`
- [ ] L49 `getByte(String)`: no description, @param key, @return  
  `public byte getByte(String key)`
- [ ] L55 `getByteArray(String)`: no description, @param key, @return  
  `public byte[] getByteArray(String key)`
- [ ] L61 `getCompound(String)`: no description, @param key, @return  
  `public NbtCompound getCompound(String key)`
- [ ] L67 `getDouble(String)`: no description, @param key, @return  
  `public double getDouble(String key)`
- [ ] L73 `getFloat(String)`: no description, @param key, @return  
  `public float getFloat(String key)`
- [ ] L79 `getInt(String)`: no description, @param key, @return  
  `public int getInt(String key)`
- [ ] L85 `getIntArray(String)`: no description, @param key, @return  
  `public int[] getIntArray(String key)`
- [ ] L91 `getList(String)`: no description, @param key, @return  
  `public NbtList getList(String key)`
- [ ] L97 `getLong(String)`: no description, @param key, @return  
  `public long getLong(String key)`
- [ ] L103 `getLongArray(String)`: no description, @param key, @return  
  `public long[] getLongArray(String key)`
- [ ] L109 `getShort(String)`: no description, @param key, @return  
  `public short getShort(String key)`
- [ ] L115 `getString(String)`: no description, @param key, @return  
  `public String getString(String key)`
- [ ] L122 `has(String)`: no description, @param key, @return  
  `public boolean has(String key) { return entries.containsKey(key); }`
- [ ] L128 `remove(String)`: @param key  
  `public Optional<NbtTag> remove(String key)`
- [ ] L132 `asMap()`: no description, @return  
  `public Map<String, NbtTag> asMap()`
- [ ] L140 `copy()`: @return  
  `public NbtCompound copy()`

## nbt.tag.NbtDouble

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtDouble.java`

- [ ] L6 `NbtDouble(double)`: @param value  
  `public record NbtDouble(double value) implements NbtTag`
- [ ] L6 `NbtDouble`: @param value (record component), no description  
  `public record NbtDouble(double value) implements NbtTag`
- [ ] L7 `id()`: @return  
  `public byte id() { return 6; }`

## nbt.tag.NbtEnd

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtEnd.java`

- [ ] L6 `NbtEnd`: no description  
  `public final class NbtEnd implements NbtTag`
- [ ] L9 `id()`: @return  
  `public byte id() { return 0; }`

## nbt.tag.NbtFloat

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtFloat.java`

- [ ] L6 `NbtFloat(float)`: @param value  
  `public record NbtFloat(float value) implements NbtTag`
- [ ] L6 `NbtFloat`: @param value (record component), no description  
  `public record NbtFloat(float value) implements NbtTag`
- [ ] L7 `id()`: @return  
  `public byte id() { return 5; }`

## nbt.tag.NbtInt

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtInt.java`

- [ ] L6 `NbtInt(int)`: @param value  
  `public record NbtInt(int value) implements NbtTag`
- [ ] L6 `NbtInt`: @param value (record component), no description  
  `public record NbtInt(int value) implements NbtTag`
- [ ] L7 `id()`: @return  
  `public byte id() { return 3; }`

## nbt.tag.NbtIntArray

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtIntArray.java`

- [ ] L6 `NbtIntArray(int[])`: @param value  
  `public record NbtIntArray(int[] value) implements NbtTag`
- [ ] L6 `NbtIntArray`: @param value (record component), no description  
  `public record NbtIntArray(int[] value) implements NbtTag`
- [ ] L7 `id()`: no description, @return  
  `public byte id() { return 11; }`
- [ ] L9 `copy()`: @return  
  `public NbtIntArray copy() { return new NbtIntArray(value.clone()); }`

## nbt.tag.NbtList

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtList.java`

- [ ] L8 `NbtList`: no description  
  `public final class NbtList implements NbtTag`
- [ ] L19 `id()`: no description, @return  
  `public byte id() { return 9; }`
- [ ] L20 `elementId()`: no description, @return  
  `public byte elementId() { return elementId; }`
- [ ] L22 `add(NbtTag)`: no description, @param tag  
  `public void add(NbtTag tag)`
- [ ] L29 `elements()`: no description, @return  
  `public List<NbtTag> elements()`
- [ ] L33 `size()`: no description, @return  
  `public int size() { return elements.size(); }`
- [ ] L36 `copy()`: @return  
  `public NbtList copy()`

## nbt.tag.NbtLong

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtLong.java`

- [ ] L6 `NbtLong(long)`: @param value  
  `public record NbtLong(long value) implements NbtTag`
- [ ] L6 `NbtLong`: @param value (record component), no description  
  `public record NbtLong(long value) implements NbtTag`
- [ ] L7 `id()`: @return  
  `public byte id() { return 4; }`

## nbt.tag.NbtLongArray

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtLongArray.java`

- [ ] L6 `NbtLongArray(long[])`: @param value  
  `public record NbtLongArray(long[] value) implements NbtTag`
- [ ] L6 `NbtLongArray`: @param value (record component), no description  
  `public record NbtLongArray(long[] value) implements NbtTag`
- [ ] L7 `id()`: no description, @return  
  `public byte id() { return 12; }`
- [ ] L9 `copy()`: @return  
  `public NbtLongArray copy() { return new NbtLongArray(value.clone()); }`

## nbt.tag.NbtShort

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtShort.java`

- [ ] L6 `NbtShort(short)`: @param value  
  `public record NbtShort(short value) implements NbtTag`
- [ ] L6 `NbtShort`: @param value (record component), no description  
  `public record NbtShort(short value) implements NbtTag`
- [ ] L7 `id()`: @return  
  `public byte id() { return 2; }`

## nbt.tag.NbtString

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtString.java`

- [ ] L6 `NbtString(String)`: @param value  
  `public record NbtString(String value) implements NbtTag`
- [ ] L6 `NbtString`: @param value (record component), no description  
  `public record NbtString(String value) implements NbtTag`
- [ ] L7 `id()`: @return  
  `public byte id() { return 8; }`

## nbt.tag.NbtTag

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/NbtTag.java`

- [ ] L6 `NbtTag`: no description  
  `public sealed interface NbtTag`
- [ ] L14 `id()`: no description, @return  
  `byte id();`
- [ ] L19 `copy()`: @return  
  `default NbtTag copy()`

## nbt.tag.package-info

`minified-utils/src/main/java/com/dervarex/minified/utils/nbt/tag/package-info.java`

- [ ] package `com.dervarex.minified.utils.nbt.tag` has no package description

## network.NetworkUtil

`minified-utils/src/main/java/com/dervarex/minified/utils/network/NetworkUtil.java`

- [ ] L11 `NetworkUtil`: no description  
  `public class NetworkUtil`

## os.OS

`minified-utils/src/main/java/com/dervarex/minified/utils/os/OS.java`

- [ ] L6 `OS`: no description  
  `public enum OS`
- [ ] L26 `toString()`: no description, @return  
  `public String toString()`
- [ ] L30 `getCurrentOS()`: no description, @return  
  `public static OS getCurrentOS()`

## os.package-info

`minified-utils/src/main/java/com/dervarex/minified/utils/os/package-info.java`

- [ ] package `com.dervarex.minified.utils.os` has no package description

## sha.Hasher

`minified-utils/src/main/java/com/dervarex/minified/utils/sha/Hasher.java`

- [ ] L19 `sha1(Path)`: no description, @param path, @return, @throws ?  
  `public static String sha1(Path path) throws IOException`
- [ ] L37 `bytesToHex(byte[])`: no description, @param bytes, @return  
  `return bytesToHex(digest.digest());`
- [ ] L40 `sha(Path,int)`: no description, @param path, @param hashbits, @return, @throws ?  
  `public static String sha(Path path, int hashbits) throws Exception`

## shortcut.Shortcut

`minified-utils/src/main/java/com/dervarex/minified/utils/shortcut/Shortcut.java`

- [ ] L12 `Shortcut`: no description  
  `public final class Shortcut`
- [ ] L36 `write()`: no description, @return, @throws ?  
  `public Path write() throws IOException`
- [ ] L40 `name()`: no description, @return  
  `public String name() { return name; }`
- [ ] L41 `shortcutPath()`: no description, @return  
  `public Path shortcutPath() { return shortcutPath; }`
- [ ] L42 `execPath()`: no description, @return  
  `public Path execPath() { return execPath; }`
- [ ] L43 `directory()`: no description, @return  
  `public Path directory() { return directory; }`
- [ ] L44 `icon()`: no description, @return  
  `public Path icon() { return icon; }`
- [ ] L45 `os()`: no description, @return  
  `public OS os() { return os; }`
- [ ] L46 `arguments()`: no description, @return  
  `public String arguments() { return arguments; }`
- [ ] L47 `terminal()`: no description, @return  
  `public boolean terminal() { return terminal; }`
- [ ] L48 `comment()`: no description, @return  
  `public String comment() { return comment; }`
- [ ] L50 `builder()`: no description, @return  
  `public static Builder builder() { return new Builder(); }`
- [ ] L52 `Shortcut.Builder`: no description  
  `public static final class Builder`
- [ ] L113 `terminal(boolean)`: no description, @param t, @return  
  `public Builder terminal(boolean t) { this.terminal = t; return this; }`
- [ ] L122 `build()`: no description, @return  
  `public Shortcut build()`

## shortcut.package-info

`minified-utils/src/main/java/com/dervarex/minified/utils/shortcut/package-info.java`

- [ ] package `com.dervarex.minified.utils.shortcut` has no package description

## shortcut.writer.ShortcutSupport

`minified-utils/src/main/java/com/dervarex/minified/utils/shortcut/writer/ShortcutSupport.java`

- [ ] L8 `ShortcutSupport`: no description  
  `public final class ShortcutSupport`
- [ ] L12 `deriveName(Path)`: no description, @param execPath, @return  
  `public static String deriveName(Path execPath)`
- [ ] L18 `defaultShortcutPath(OS,String)`: no description, @param os, @param name, @return  
  `public static Path defaultShortcutPath(OS os, String name)`
- [ ] L19 `sanitize(String)`: no description, @param s, @return  
  `String safe = sanitize(name);`
- [ ] L34 `escapeDesktop(String)`: no description, @param s, @return  
  `public static String escapeDesktop(String s)`
- [ ] L41 `desktopExecQuote(String)`: @param s, @return  
  `public static String desktopExecQuote(String s)`
- [ ] L50 `shellQuote(String)`: no description, @param s, @return  
  `public static String shellQuote(String s)`
- [ ] L54 `psQuote(String)`: no description, @param s, @return  
  `public static String psQuote(String s)`

## shortcut.writer.ShortcutWriter

`minified-utils/src/main/java/com/dervarex/minified/utils/shortcut/writer/ShortcutWriter.java`

- [ ] L9 `ShortcutWriter`: no description  
  `public interface ShortcutWriter`
- [ ] L11 `write(Shortcut)`: no description, @param shortcut, @return, @throws ?  
  `Path write(Shortcut shortcut) throws IOException;`
- [ ] L16 `forOS(OS)`: @param os, @return  
  `static ShortcutWriter forOS(OS os)`

## shortcut.writer.package-info

`minified-utils/src/main/java/com/dervarex/minified/utils/shortcut/writer/package-info.java`

- [ ] package `com.dervarex.minified.utils.shortcut.writer` has no package description

## version.VersionListProvider

`minified-utils/src/main/java/com/dervarex/minified/utils/version/VersionListProvider.java`

- [ ] L9 `VersionListProvider`: no description  
  `public final class VersionListProvider`
- [ ] L13 `getVersions()`: no description, @return, @throws ?  
  `public static ArrayList<String> getVersions() throws HttpException, IOException`
- [ ] L21 `getReleaseVersions()`: no description, @return, @throws ?  
  `public static ArrayList<String> getReleaseVersions() throws HttpException, IOException`
- [ ] L37 `getLatestReleaseVersion()`: no description, @return, @throws ?  
  `public static String getLatestReleaseVersion() throws HttpException, IOException`
- [ ] L42 `getLatestSnapshotVersion()`: no description, @return, @throws ?  
  `public static String getLatestSnapshotVersion() throws HttpException, IOException`

## version.VersionManifestClient

`minified-utils/src/main/java/com/dervarex/minified/utils/version/VersionManifestClient.java`

- [ ] L12 `VersionManifestClient`: no description  
  `public final class VersionManifestClient`
- [ ] L24 `getManifest()`: no description  
  `public static JsonFile getManifest() throws HttpException, IOException`
- [ ] L34 `getVersions()`: no description  
  `public static Iterable<JsonValue> getVersions() throws HttpException, IOException`
- [ ] L38 `getVersionEntry(String)`: no description, @param version, @return, @throws ?  
  `public static JsonValue getVersionEntry(String version) throws HttpException, IOException`

## version.VersionMetadataProvider

`minified-utils/src/main/java/com/dervarex/minified/utils/version/VersionMetadataProvider.java`

- [ ] L9 `VersionMetadataProvider`: no description  
  `public final class VersionMetadataProvider`
- [ ] L13 `getVersionJsonUrl(String)`: no description, @param version, @return, @throws ?  
  `public static String getVersionJsonUrl(String version) throws HttpException, IOException`
- [ ] L21 `getMainClass(String)`: no description, @param version, @return, @throws ?  
  `public static String getMainClass(String version) throws HttpException, IOException`
- [ ] L29 `getVersionType(String)`: no description, @param version, @return, @throws ?  
  `public static String getVersionType(String version) throws HttpException, IOException`
- [ ] L37 `getReleaseTime(String)`: no description, @param version, @return, @throws ?  
  `public static String getReleaseTime(String version) throws HttpException, IOException`
- [ ] L45 `getClientSha1(String)`: no description, @param version, @return, @throws ?  
  `public static String getClientSha1(String version) throws HttpException, IOException`
- [ ] L60 `getClientUrl(String)`: no description, @param version, @return, @throws ?  
  `public static String getClientUrl(String version)`
- [ ] L77 `getServerSha1(String)`: no description, @param version, @return, @throws ?  
  `public static String getServerSha1(String version)`
- [ ] L92 `getServerUrl(String)`: no description, @param version, @return, @throws ?  
  `public static String getServerUrl(String version)`

