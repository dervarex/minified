## com.dervarex.minified.launch.arguments.GameArgumentsParser

`minified-launch/src/main/java/com/dervarex/minified/launch/arguments/GameArgumentsParser.java`

- [ ] L12 `GameArgumentsParser`: no description  
  `public final class GameArgumentsParser`
- [ ] L18 `parse(JsonArray,Map,Map)`: @param arguments, @param variables, @param features, @return  
  `public static List<String> parse(`

## com.dervarex.minified.launch.arguments.JvmArgumentsParser

`minified-launch/src/main/java/com/dervarex/minified/launch/arguments/JvmArgumentsParser.java`

- [ ] L9 `JvmArgumentsParser`: no description  
  `public final class JvmArgumentsParser`

## com.dervarex.minified.launch.arguments.LegacyMinecraftArgumentsParser

`minified-launch/src/main/java/com/dervarex/minified/launch/arguments/LegacyMinecraftArgumentsParser.java`

- [ ] L8 `LegacyMinecraftArgumentsParser`: no description  
  `public final class LegacyMinecraftArgumentsParser`
- [ ] L14 `parse(String)`: @param minecraftArguments, @return  
  `public static List<String> parse(String minecraftArguments)`

## com.dervarex.minified.launch.download.ClientDownloader

`minified-launch/src/main/java/com/dervarex/minified/launch/download/ClientDownloader.java`

- [ ] L26 `ClientDownloader`: no description  
  `public class ClientDownloader`
- [ ] L34 `downloadClient(String,Path,Consumer,LaunchContext)`: @throws ?  
  `public void downloadClient(String version, Path path, @Nullable LaunchContext context)`
- [ ] L34 `downloadClient(String,Path,LaunchContext)`: @throws ?  
  `public void downloadClient(String version, Path path, @Nullable LaunchContext context)`

## com.dervarex.minified.launch.download.assets.AssetDownloader

`minified-launch/src/main/java/com/dervarex/minified/launch/download/assets/AssetDownloader.java`

- [ ] L35 `AssetDownloader`: no description  
  `public class AssetDownloader`
- [ ] L44 `AssetDownloader(int)`: @param downloadThreads  
  `public AssetDownloader(int downloadThreads)`
- [ ] L48 `downloadAssets(String,Path,LaunchContext)`: no description, @param version, @param assetsDir, @param context  
  `public void downloadAssets(String version, Path assetsDir, LaunchContext context)`

## com.dervarex.minified.launch.download.assets.AssetOfflineValidator

`minified-launch/src/main/java/com/dervarex/minified/launch/download/assets/AssetOfflineValidator.java`

- [ ] L14 `AssetOfflineValidator`: no description  
  `public final class AssetOfflineValidator`

## com.dervarex.minified.launch.download.assets.LegacyAssets

`minified-launch/src/main/java/com/dervarex/minified/launch/download/assets/LegacyAssets.java`

- [ ] L27 `resolveGameAssetsDirectory(JsonFile,LaunchConfiguration)`: no description, @param versionJson, @param config  
  `public static Path resolveGameAssetsDirectory(JsonFile versionJson, LaunchConfiguration config)`

## com.dervarex.minified.launch.download.libraries.LibraryDownloader

`minified-launch/src/main/java/com/dervarex/minified/launch/download/libraries/LibraryDownloader.java`

- [ ] L53 `LibraryDownloader`: no description  
  `public class LibraryDownloader`

## com.dervarex.minified.launch.download.libraries.OfflineLibraryValidator

`minified-launch/src/main/java/com/dervarex/minified/launch/download/libraries/OfflineLibraryValidator.java`

- [ ] L25 `OfflineLibraryValidator`: no description  
  `public final class OfflineLibraryValidator`

## com.dervarex.minified.launch.events.download.assets.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/events/download/assets/package-info.java`

- [ ] package `com.dervarex.minified.launch.events.download.assets` has no package description

## com.dervarex.minified.launch.events.download.client.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/events/download/client/package-info.java`

- [ ] package `com.dervarex.minified.launch.events.download.client` has no package description

## com.dervarex.minified.launch.events.download.libraries.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/events/download/libraries/package-info.java`

- [ ] package `com.dervarex.minified.launch.events.download.libraries` has no package description

## com.dervarex.minified.launch.events.environment.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/events/environment/package-info.java`

- [ ] package `com.dervarex.minified.launch.events.environment` has no package description

## com.dervarex.minified.launch.events.launch.GameOutputEvent

`minified-launch/src/main/java/com/dervarex/minified/launch/events/launch/GameOutputEvent.java`

- [ ] L15 `GameOutputEvent.Stream`: no description  
  `public enum Stream`

## com.dervarex.minified.launch.events.launch.NonZeroExitCodeEvent

`minified-launch/src/main/java/com/dervarex/minified/launch/events/launch/NonZeroExitCodeEvent.java`

- [ ] L10 `NonZeroExitCodeEvent(int)`: @param exitCode  
  `public record NonZeroExitCodeEvent(int exitCode) implements Event`
- [ ] L10 `NonZeroExitCodeEvent`: @param exitCode (record component)  
  `public record NonZeroExitCodeEvent(int exitCode) implements Event`

## com.dervarex.minified.launch.events.launch.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/events/launch/package-info.java`

- [ ] package `com.dervarex.minified.launch.events.launch` has no package description

## com.dervarex.minified.launch.events.loader.InstallForgeEvent

`minified-launch/src/main/java/com/dervarex/minified/launch/events/loader/InstallForgeEvent.java`

- [ ] L20 `InstallForgeEvent.Stage`: no description  
  `public enum Stage`

## com.dervarex.minified.launch.events.loader.InstallNeoforgeEvent

`minified-launch/src/main/java/com/dervarex/minified/launch/events/loader/InstallNeoforgeEvent.java`

- [ ] L20 `InstallNeoforgeEvent.Stage`: no description  
  `public enum Stage`

## com.dervarex.minified.launch.events.loader.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/events/loader/package-info.java`

- [ ] package `com.dervarex.minified.launch.events.loader` has no package description

## com.dervarex.minified.launch.launch.LaunchConfiguration

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/LaunchConfiguration.java`

- [ ] L86 `getExtraJvmArgs()`: no description, @return  
  `public List<String> getExtraJvmArgs()`
- [ ] L114 `LaunchConfiguration.Builder`: no description  
  `public static class Builder`
- [ ] L124 `minRam(int)`: no description, @param minRam, @return  
  `public Builder minRam(int minRam)`
- [ ] L129 `maxRam(int)`: no description, @param maxRam, @return  
  `public Builder maxRam(int maxRam)`
- [ ] L135 `downloadThreads(int)`: no description, @param threads, @return  
  `public Builder downloadThreads(int threads)`
- [ ] L141 `resolution(int,int)`: no description, @param width, @param height, @return  
  `public Builder resolution(int width, int height)`
- [ ] L149 `launcherName(String)`: no description, @param name, @return  
  `public Builder launcherName(String name)`
- [ ] L154 `launcherVersion(String)`: no description, @param version, @return  
  `public Builder launcherVersion(String version)`
- [ ] L160 `isDemoUser(boolean)`: no description, @param demo, @return  
  `public Builder isDemoUser(boolean demo)`
- [ ] L166 `jarFile(Path)`: no description, @param jarFile, @return  
  `public Builder jarFile(Path jarFile)`
- [ ] L171 `librariesDirectory(Path)`: no description, @param librariesDirectory, @return  
  `public Builder librariesDirectory(Path librariesDirectory)`
- [ ] L176 `assetsDirectory(Path)`: no description, @param assetsDirectory, @return  
  `public Builder assetsDirectory(Path assetsDirectory)`
- [ ] L181 `nativesDirectory(Path)`: no description, @param nativesDirectory, @return  
  `public Builder nativesDirectory(Path nativesDirectory)`
- [ ] L192 `gameDirectory(Path)`: @return  
  `public Builder gameDirectory(Path gameDirectory)`
- [ ] L197 `customJavaExecutable(Path)`: no description, @param customJavaExecutable, @return  
  `public Builder customJavaExecutable(Path customJavaExecutable)`
- [ ] L203 `extraJvmArg(String)`: no description, @param arg, @return  
  `public Builder extraJvmArg(String arg)`
- [ ] L208 `extraJvmArgs(List)`: no description, @param args, @return  
  `public Builder extraJvmArgs(List<String> args)`
- [ ] L215 `loader(Loader)`: no description, @param loader, @return  
  `public Builder loader(Loader loader)`
- [ ] L228 `captureGameOutput(boolean)`: @return  
  `public Builder captureGameOutput(boolean captureGameOutput)`
- [ ] L234 `headless(boolean)`: no description, @param headless, @return  
  `public Builder headless(boolean headless)`
- [ ] L239 `headlessTimeoutSeconds(int)`: no description, @param headlessTimeoutSeconds, @return  
  `public Builder headlessTimeoutSeconds(int headlessTimeoutSeconds)`
- [ ] L244 `headlessMarker(String)`: no description, @param headlessMarker, @return  
  `public Builder headlessMarker(String headlessMarker)`
- [ ] L255 `offlineUsername(String)`: @return  
  `public Builder offlineUsername(String username)`
- [ ] L261 `eventBus(EventBus)`: no description, @param eventBus, @return  
  `public Builder eventBus(EventBus eventBus)`
- [ ] L266 `build()`: no description, @return  
  `public LaunchConfiguration build()`

## com.dervarex.minified.launch.launch.LaunchContext

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/LaunchContext.java`

- [ ] L35 `LaunchContext(User,LaunchConfiguration)`: @param user, @param launchConfiguration  
  `public LaunchContext(User user, LaunchConfiguration launchConfiguration)`
- [ ] L44 `requestStop(boolean)`: @param force  
  `public synchronized void requestStop(boolean force)`
- [ ] L60 `isStopRequested()`: no description, @return  
  `public boolean isStopRequested()`
- [ ] L67 `throwIfStopRequested()`: no description  
  `public void throwIfStopRequested()`
- [ ] L76 `runInterruptibly(Runnable)`: @param step  
  `public void runInterruptibly(Runnable step)`
- [ ] L97 `startProcess(ProcessBuilder)`: @param processBuilder, @return, @throws ?  
  `public synchronized Process startProcess(ProcessBuilder processBuilder) throws IOException`
- [ ] L103 `isProcessStarted()`: no description, @return  
  `public synchronized boolean isProcessStarted()`

## com.dervarex.minified.launch.launch.Launcher

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/Launcher.java`

- [ ] L63 `Launcher`: no description  
  `public class Launcher`
- [ ] L429 `Launcher.ProfileSupplier`: no description  
  `public interface ProfileSupplier`
- [ ] L430 `get()`: @return  
  `JsonObject get();`

## com.dervarex.minified.launch.launch.internal.ArgumentsBuilder

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/internal/ArgumentsBuilder.java`

- [ ] L28 `ArgumentsBuilder`: no description  
  `public class ArgumentsBuilder`
- [ ] L29 `buildJvmArguments(JsonFile,LaunchConfiguration,LaunchOptions,Loader,String,boolean)`: no description, @param versionJson, @param launchConfig, @param options, @param loader, @param version, @param online, @return  
  `public static List<String> buildJvmArguments(`
- [ ] L102 `buildGameArguments(JsonFile,LaunchOptions,Loader,String,LaunchConfiguration,boolean)`: no description, @param versionJson, @param options, @param loader, @param version, @param launchConfig, @param online, @return  
  `public static List<String> buildGameArguments(`

## com.dervarex.minified.launch.launch.internal.CacheManager

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/internal/CacheManager.java`

- [ ] L20 `CacheManager`: no description  
  `public class CacheManager`
- [ ] L44 `loadProfileJson(String,String,String,boolean,ProfileSupplier)`: @param version, @param loaderName, @param loaderVersion, @param online, @param supplier, @return  
  `public static JsonObject loadProfileJson(`
- [ ] L76 `loadVersionJson(String,boolean)`: no description, @param version, @param online, @return, @throws ?  
  `public static JsonFile loadVersionJson(String version, boolean online) throws IOException`

## com.dervarex.minified.launch.launch.internal.ClasspathBuilder

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/internal/ClasspathBuilder.java`

- [ ] L31 `ClasspathBuilder`: no description  
  `public class ClasspathBuilder`
- [ ] L32 `buildClasspath(JsonFile,LaunchConfiguration,boolean)`: @param versionJson, @param config, @param online, @return  
  `public static String buildClasspath(JsonFile versionJson, LaunchConfiguration config, boolean online)`

## com.dervarex.minified.launch.launch.internal.HeadlessWatcher

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/internal/HeadlessWatcher.java`

- [ ] L60 `forLaunch(LaunchConfiguration,boolean)`: no description, @param launchConfig, @return  
  `public static HeadlessWatcher forLaunch(LaunchConfiguration launchConfig, boolean logsNoMarkers)`
- [ ] L74 `watch(Process)`: @param process, @throws ?  
  `public int watch(Process process) throws InterruptedException`

## com.dervarex.minified.launch.launch.internal.LaunchOptions

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/internal/LaunchOptions.java`

- [ ] L51 `buildLaunchOptions(User,String,LaunchConfiguration,JsonFile,String)`: no description, @param user, @param version, @param launchConfig, @param versionJson, @param classpathString, @return  
  `public static LaunchOptions buildLaunchOptions(User user, String version, LaunchConfiguration launchConfig, JsonFile versionJson, String classpathString)`

## com.dervarex.minified.launch.launch.internal.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/internal/package-info.java`

- [ ] package `com.dervarex.minified.launch.launch.internal` has no package description

## com.dervarex.minified.launch.profile.ProfileFactory

`minified-launch/src/main/java/com/dervarex/minified/launch/profile/ProfileFactory.java`

- [ ] L29 `ProfileFactory`: no description  
  `public class ProfileFactory`

## com.dervarex.minified.launch.utils.X11Helper

`minified-launch/src/main/java/com/dervarex/minified/launch/utils/X11Helper.java`

- [ ] L14 `X11Helper`: no description  
  `public class X11Helper`
- [ ] L20 `configureGraphicsEnvironment(ProcessBuilder,LaunchContext)`: @param context  
  `public static void configureGraphicsEnvironment(`
- [ ] L20 `configureGraphicsEnvironment(ProcessBuilder,Path,LaunchContext)`: @param processBuilder, @param x11SocketDirectory, @param context  
  `public static void configureGraphicsEnvironment(`
- [ ] L133 `substituteVariables(List,Map)`: @param arguments, @param variables, @return  
  `public static List<String> substituteVariables(`

