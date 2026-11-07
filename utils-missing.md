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

