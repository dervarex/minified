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

