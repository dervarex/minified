## com.dervarex.minified.events.EventBus

`minified-events/src/main/java/com/dervarex/minified/events/EventBus.java`

- [ ] L11 `EventBus`: no description  
  `public final class EventBus`
- [ ] L24 `subscribe(Class,EventListener)`: @param type, @param listener  
  `public <T extends Event> void subscribe(Class<T> type, EventListener<T> listener)`
- [ ] L59 `post(Event)`: @param event  
  `public <T extends Event> void post(T event)`

## com.dervarex.minified.events.EventListener

`minified-events/src/main/java/com/dervarex/minified/events/EventListener.java`

- [ ] L7 `EventListener`: no description  
  `public interface EventListener<T extends Event>`
- [ ] L8 `onEvent(Event)`: @param event  
  `void onEvent(T event);`

## com.dervarex.minified.events.package-info

`minified-events/src/main/java/com/dervarex/minified/events/package-info.java`

- [ ] package `com.dervarex.minified.events` has no package description

## com.dervarex.minified.events.type.connection.package-info

`minified-events/src/main/java/com/dervarex/minified/events/type/connection/package-info.java`

- [ ] package `com.dervarex.minified.events.type.connection` has no package description

## com.dervarex.minified.java.JavaManager

`minified-java/src/main/java/com/dervarex/minified/java/JavaManager.java`

- [ ] L105 `getBaseDir()`: no description  
  `public static synchronized Path getBaseDir()`

## com.dervarex.minified.java.events.download.package-info

`minified-java/src/main/java/com/dervarex/minified/java/events/download/package-info.java`

- [ ] package `com.dervarex.minified.java.events.download` has no package description

## com.dervarex.minified.java.events.extract.ArchiveType

`minified-java/src/main/java/com/dervarex/minified/java/events/extract/ArchiveType.java`

- [ ] L6 `ArchiveType`: no description  
  `public enum ArchiveType`

## com.dervarex.minified.java.events.extract.package-info

`minified-java/src/main/java/com/dervarex/minified/java/events/extract/package-info.java`

- [ ] package `com.dervarex.minified.java.events.extract` has no package description

## com.dervarex.minified.java.events.package-info

`minified-java/src/main/java/com/dervarex/minified/java/events/package-info.java`

- [ ] package `com.dervarex.minified.java.events` has no package description

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

## com.dervarex.minified.launch.exceptions.cache.FailedToCacheException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/cache/FailedToCacheException.java`

- [ ] L10 `FailedToCacheException(String,Throwable)`: @param message, @param cause  
  `public FailedToCacheException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.cache.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/cache/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.cache` has no package description

## com.dervarex.minified.launch.exceptions.download.AssetDownloadException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/download/AssetDownloadException.java`

- [ ] L10 `AssetDownloadException(String)`: @param message  
  `public AssetDownloadException(String message)`
- [ ] L10 `AssetDownloadException(Throwable)`: @param cause  
  `public AssetDownloadException(String message)`
- [ ] L16 `AssetDownloadException(String,Throwable)`: @param message, @param cause  
  `public AssetDownloadException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.download.DownloadException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/download/DownloadException.java`

- [ ] L10 `DownloadException(String)`: @param message  
  `public DownloadException(String message)`
- [ ] L10 `DownloadException(Throwable)`: @param cause  
  `public DownloadException(String message)`
- [ ] L16 `DownloadException(String,Throwable)`: @param message, @param cause  
  `public DownloadException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.download.DownloadPoolShutdownException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/download/DownloadPoolShutdownException.java`

- [ ] L10 `DownloadPoolShutdownException(String)`: @param message  
  `public DownloadPoolShutdownException(String message)`
- [ ] L10 `DownloadPoolShutdownException(Throwable)`: @param cause  
  `public DownloadPoolShutdownException(String message)`
- [ ] L16 `DownloadPoolShutdownException(String,Throwable)`: @param message, @param cause  
  `public DownloadPoolShutdownException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.download.DownloadPoolTimeoutException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/download/DownloadPoolTimeoutException.java`

- [ ] L10 `DownloadPoolTimeoutException(String)`: @param message  
  `public DownloadPoolTimeoutException(String message)`
- [ ] L10 `DownloadPoolTimeoutException(Throwable)`: @param cause  
  `public DownloadPoolTimeoutException(String message)`
- [ ] L16 `DownloadPoolTimeoutException(String,Throwable)`: @param message, @param cause  
  `public DownloadPoolTimeoutException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.download.HttpDownloadException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/download/HttpDownloadException.java`

- [ ] L15 `HttpDownloadException(String,int)`: @param message, @param statusCode  
  `public HttpDownloadException(String message, int statusCode)`
- [ ] L20 `HttpDownloadException(String,int,Throwable)`: @param message, @param statusCode, @param cause  
  `public HttpDownloadException(String message, int statusCode, Throwable cause)`

## com.dervarex.minified.launch.exceptions.download.LibraryDownloadException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/download/LibraryDownloadException.java`

- [ ] L12 `LibraryDownloadException(String)`: @param message  
  `public LibraryDownloadException(String message)`
- [ ] L12 `LibraryDownloadException(Throwable)`: @param cause  
  `public LibraryDownloadException(String message)`
- [ ] L18 `LibraryDownloadException(String,Throwable)`: @param message, @param cause  
  `public LibraryDownloadException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.download.NativeExtractionException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/download/NativeExtractionException.java`

- [ ] L10 `NativeExtractionException(String)`: @param message  
  `public NativeExtractionException(String message)`
- [ ] L10 `NativeExtractionException(Throwable)`: @param cause  
  `public NativeExtractionException(String message)`
- [ ] L16 `NativeExtractionException(String,Throwable)`: @param message, @param cause  
  `public NativeExtractionException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.download.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/download/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.download` has no package description

## com.dervarex.minified.launch.exceptions.launch.LaunchFailedException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/launch/LaunchFailedException.java`

- [ ] L10 `LaunchFailedException(String,Throwable)`: @param message, @param cause  
  `public LaunchFailedException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.launch.NonZeroExitCodeException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/launch/NonZeroExitCodeException.java`

- [ ] L13 `NonZeroExitCodeException(String,int)`: @param message, @param exitCode  
  `public NonZeroExitCodeException(String message, int exitCode)`

## com.dervarex.minified.launch.exceptions.launch.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/launch/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.launch` has no package description

## com.dervarex.minified.launch.exceptions.libraries.FailedToLoadLibrariesException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/libraries/FailedToLoadLibrariesException.java`

- [ ] L17 `FailedToLoadLibrariesException(String,Loader)`: @param message, @param loader  
  `public FailedToLoadLibrariesException(String message, Loader loader)`
- [ ] L22 `FailedToLoadLibrariesException(String,Loader,Throwable)`: @param message, @param loader, @param cause  
  `public FailedToLoadLibrariesException(String message, Loader loader, Throwable cause)`

## com.dervarex.minified.launch.exceptions.libraries.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/libraries/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.libraries` has no package description

## com.dervarex.minified.launch.exceptions.loader.FailedToLoadLoaderProfileException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/FailedToLoadLoaderProfileException.java`

- [ ] L17 `FailedToLoadLoaderProfileException(String,String,Throwable)`: @param message, @param loader, @param cause  
  `public FailedToLoadLoaderProfileException(String message, String loader, Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/NoLoadersFoundException.java`

- [ ] L18 `NoLoadersFoundException(String,String)`: @param message, @param loader  
  `public NoLoadersFoundException(String message, String loader)`
- [ ] L23 `NoLoadersFoundException(String,String,Throwable)`: @param message, @param loader, @param cause  
  `public NoLoadersFoundException(String message, String loader, Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/UnexpectedLoaderException.java`

- [ ] L10 `UnexpectedLoaderException(String)`: @param message  
  `public UnexpectedLoaderException(String message)`
- [ ] L10 `UnexpectedLoaderException(Throwable)`: @param cause  
  `public UnexpectedLoaderException(String message)`
- [ ] L16 `UnexpectedLoaderException(String,Throwable)`: @param message, @param cause  
  `public UnexpectedLoaderException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.forge.FailedToFetchPromotionsException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/forge/FailedToFetchPromotionsException.java`

- [ ] L11 `FailedToFetchPromotionsException(String)`: @param message  
  `public FailedToFetchPromotionsException(String message)`
- [ ] L15 `FailedToFetchPromotionsException(String,Throwable)`: @param message, @param cause  
  `public FailedToFetchPromotionsException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.forge.FailedToRunForgeInstallerException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/forge/FailedToRunForgeInstallerException.java`

- [ ] L10 `FailedToRunForgeInstallerException(Throwable)`: @param cause  
  `public FailedToRunForgeInstallerException(Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.forge.ForgeInstallerReportFailureException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/forge/ForgeInstallerReportFailureException.java`

- [ ] L10 `ForgeInstallerReportFailureException(String)`: @param s  
  `public ForgeInstallerReportFailureException(String s)`

## com.dervarex.minified.launch.exceptions.loader.forge.ForgePreparationException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/forge/ForgePreparationException.java`

- [ ] L10 `ForgePreparationException(Throwable)`: @param cause  
  `public ForgePreparationException(Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.forge.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/forge/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.loader.forge` has no package description

## com.dervarex.minified.launch.exceptions.loader.neoforge.FailedToReadMetadataException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/neoforge/FailedToReadMetadataException.java`

- [ ] L10 `FailedToReadMetadataException(String)`: @param message  
  `public FailedToReadMetadataException(String message)`
- [ ] L13 `FailedToReadMetadataException(String,Throwable)`: @param message, @param cause  
  `public FailedToReadMetadataException(String message, Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.neoforge.FailedToRunNeoforgeInstallerException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/neoforge/FailedToRunNeoforgeInstallerException.java`

- [ ] L10 `FailedToRunNeoforgeInstallerException(Throwable)`: @param cause  
  `public FailedToRunNeoforgeInstallerException(Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.neoforge.MalformedMetadataException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/neoforge/MalformedMetadataException.java`

- [ ] L10 `MalformedMetadataException(String)`: @param message  
  `public MalformedMetadataException(String message)`

## com.dervarex.minified.launch.exceptions.loader.neoforge.NeoforgeInstallerReportFailureException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/neoforge/NeoforgeInstallerReportFailureException.java`

- [ ] L10 `NeoforgeInstallerReportFailureException(String)`: @param s  
  `public NeoforgeInstallerReportFailureException(String s)`

## com.dervarex.minified.launch.exceptions.loader.neoforge.NeoforgePreparationException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/neoforge/NeoforgePreparationException.java`

- [ ] L10 `NeoforgePreparationException(Throwable)`: @param cause  
  `public NeoforgePreparationException(Throwable cause)`

## com.dervarex.minified.launch.exceptions.loader.neoforge.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/neoforge/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.loader.neoforge` has no package description

## com.dervarex.minified.launch.exceptions.loader.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/loader/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.loader` has no package description

## com.dervarex.minified.launch.exceptions.profile.FailedToLoadProfileException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/profile/FailedToLoadProfileException.java`

- [ ] L10 `FailedToLoadProfileException(String,Throwable)`: @param message, @param t  
  `public FailedToLoadProfileException(String message, Throwable t)`

## com.dervarex.minified.launch.exceptions.profile.FailedToSaveProfileException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/profile/FailedToSaveProfileException.java`

- [ ] L10 `FailedToSaveProfileException(String,Throwable)`: @param message, @param t  
  `public FailedToSaveProfileException(String message, Throwable t)`

## com.dervarex.minified.launch.exceptions.profile.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/profile/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.profile` has no package description

## com.dervarex.minified.launch.exceptions.version.FailedToFetchVersionsException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/version/FailedToFetchVersionsException.java`

- [ ] L17 `FailedToFetchVersionsException(String,String)`: @param message, @param loader  
  `public FailedToFetchVersionsException(String message, String loader)`
- [ ] L22 `FailedToFetchVersionsException(String,String,Throwable)`: @param message, @param loader, @param cause  
  `public FailedToFetchVersionsException(String message, String loader, Throwable cause)`

## com.dervarex.minified.launch.exceptions.version.MalformedVersionJsonException

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/version/MalformedVersionJsonException.java`

- [ ] L10 `MalformedVersionJsonException(String)`: @param message  
  `public MalformedVersionJsonException(String message)`

## com.dervarex.minified.launch.exceptions.version.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/exceptions/version/package-info.java`

- [ ] package `com.dervarex.minified.launch.exceptions.version` has no package description

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

## com.dervarex.minified.launch.launch.modding.Loader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/Loader.java`

- [ ] L13 `name()`: @return  
  `String name();`
- [ ] L14 `mcVersion()`: no description, @return  
  `String mcVersion();`
- [ ] L15 `loaderVersion()`: no description, @return  
  `String loaderVersion();`
- [ ] L16 `iconUrl()`: no description, @return  
  `String iconUrl();`

## com.dervarex.minified.launch.launch.modding.custom.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/custom/package-info.java`

- [ ] package `com.dervarex.minified.launch.launch.modding.custom` has no package description

## com.dervarex.minified.launch.launch.modding.fabric.FabricLoader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/fabric/FabricLoader.java`

- [ ] L12 `FabricLoader(String,String)`: @param mcVersion, @param loaderVersion  
  `public record FabricLoader(String mcVersion, String loaderVersion) implements Loader`
- [ ] L12 `FabricLoader`: @param mcVersion (record component), @param loaderVersion (record component)  
  `public record FabricLoader(String mcVersion, String loaderVersion) implements Loader`
- [ ] L14 `iconUrl()`: no description, @return  
  `public String iconUrl()`
- [ ] L19 `name()`: @return  
  `public String name() { return "FABRIC"; }`

## com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/fabric/FabricLoaderFetcher.java`

- [ ] L12 `FabricLoaderFetcher`: no description  
  `public class FabricLoaderFetcher`
- [ ] L18 `getLatestLoaderVersion()`: no description, @throws ?  
  `public static String getLatestLoaderVersion() throws Exception`
- [ ] L41 `getProfileJson(String,String)`: no description, @throws ?  
  `public static JsonObject getProfileJson(`
- [ ] L63 `getLatestProfile(String)`: no description, @throws ?  
  `public static JsonObject getLatestProfile(`

## com.dervarex.minified.launch.launch.modding.fabric.FabricProfileJsonLoader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/fabric/FabricProfileJsonLoader.java`

- [ ] L10 `FabricProfileJsonLoader`: no description  
  `public class FabricProfileJsonLoader`
- [ ] L12 `loadFabricProfileJson(String,LaunchConfiguration,boolean)`: @param version, @param launchConfig, @param online, @return  
  `public static JsonObject loadFabricProfileJson(String version, LaunchConfiguration launchConfig, boolean online)`

## com.dervarex.minified.launch.launch.modding.forge.ForgeLoader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/forge/ForgeLoader.java`

- [ ] L14 `iconUrl()`: no description, @return  
  `public String iconUrl()`
- [ ] L19 `name()`: @return  
  `public String name() { return "FORGE"; }`

## com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionFetcher

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/forge/api/ForgeVersionFetcher.java`

- [ ] L34 `getAvailableVersions()`: no description  
  `public List<String> getAvailableVersions()`
- [ ] L66 `getVersionsForMinecraft(String)`: no description  
  `public List<String> getVersionsForMinecraft(String minecraftVersion)`
- [ ] L74 `getLatest(String)`: no description, @param minecraftVersion, @return  
  `public String getLatest(String minecraftVersion)`
- [ ] L85 `getRecommended(String)`: no description, @param minecraftVersion, @return  
  `public String getRecommended(String minecraftVersion)`

## com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/forge/api/ForgeVersionJson.java`

- [ ] L22 `getVersionJson(Path,String)`: no description  
  `public static JsonFile getVersionJson(Path gameDir, String loaderVersion) throws IOException`
- [ ] L24 `getVersionJsonPath(Path,String)`: no description  
  `getVersionJsonPath(gameDir, loaderVersion).toFile()`
- [ ] L35 `getVersionId(String)`: no description  
  `String id = getVersionId(loaderVersion);`

## com.dervarex.minified.launch.launch.modding.forge.installer.JarModForgeInstaller

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/forge/installer/JarModForgeInstaller.java`

- [ ] L46 `install(LaunchConfiguration)`: @param config  
  `public static void install(LaunchConfiguration config)`

## com.dervarex.minified.launch.launch.modding.forge.installer.LegacyForgeGameJar

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/forge/installer/LegacyForgeGameJar.java`

- [ ] L43 `resolve(JsonObject,LaunchConfiguration)`: no description, @param config  
  `public static Path resolve(JsonObject loaderProfile, LaunchConfiguration config)`
- [ ] L57 `prepare(JsonObject,LaunchConfiguration)`: @param loaderProfile, @param config  
  `public static void prepare(JsonObject loaderProfile, LaunchConfiguration config)`

## com.dervarex.minified.launch.launch.modding.forge.installer.LegacyForgeInstaller

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/forge/installer/LegacyForgeInstaller.java`

- [ ] L49 `isLegacyInstaller(Path)`: no description  
  `public static boolean isLegacyInstaller(Path installer)`
- [ ] L54 `install(Path,LaunchConfiguration)`: no description, @param installer, @param config  
  `public static void install(Path installer, LaunchConfiguration config)`

## com.dervarex.minified.launch.launch.modding.forge.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/forge/package-info.java`

- [ ] package `com.dervarex.minified.launch.launch.modding.forge` has no package description

## com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/neoforge/NeoforgeLoader.java`

- [ ] L14 `iconUrl()`: no description, @return  
  `public String iconUrl()`
- [ ] L19 `name()`: @return  
  `public String name() { return "NEOFORGE"; }`

## com.dervarex.minified.launch.launch.modding.neoforge.api.NeoInstallerFetcher

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/neoforge/api/NeoInstallerFetcher.java`

- [ ] L7 `NeoInstallerFetcher`: no description  
  `public final class NeoInstallerFetcher`

## com.dervarex.minified.launch.launch.modding.neoforge.api.NeoProfileJsonLoader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/neoforge/api/NeoProfileJsonLoader.java`

- [ ] L13 `NeoProfileJsonLoader`: no description  
  `public class NeoProfileJsonLoader`

## com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/neoforge/api/NeoVersionFetcher.java`

- [ ] L32 `NeoVersionFetcher`: no description  
  `public final class NeoVersionFetcher`
- [ ] L46 `getLatest(String)`: no description  
  `public String getLatest(String minecraftVersion)`
- [ ] L47 `resolveLoaderVersion(String)`: no description, @param versionOrMinecraftVersion, @return  
  `return resolveLoaderVersion(minecraftVersion);`
- [ ] L52 `getAllVersions()`: no description  
  `String resolved = resolveFrom(getAllVersions(), versionOrMinecraftVersion);`
- [ ] L238 `isLegacyVersion(String)`: no description  
  `public static boolean isLegacyVersion(String neoForgeVersion)`

## com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionJson

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/neoforge/api/NeoVersionJson.java`

- [ ] L16 `NeoVersionJson`: no description  
  `public final class NeoVersionJson`
- [ ] L24 `getVersionJson(Path,String)`: @param gameDir, @param loaderVersion, @return, @throws ?  
  `public static JsonFile getVersionJson(Path gameDir, String loaderVersion) throws IOException`

## com.dervarex.minified.launch.launch.modding.neoforge.installer.NeoInstallerInjector

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/neoforge/installer/NeoInstallerInjector.java`

- [ ] L30 `NeoInstallerInjector`: no description  
  `public class NeoInstallerInjector`

## com.dervarex.minified.launch.launch.modding.neoforge.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/neoforge/package-info.java`

- [ ] package `com.dervarex.minified.launch.launch.modding.neoforge` has no package description

## com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/quilt/QuiltLoader.java`

- [ ] L7 `QuiltLoader(String,String)`: @param mcVersion, @param loaderVersion  
  `public record QuiltLoader(String mcVersion, String loaderVersion) implements Loader`
- [ ] L7 `QuiltLoader`: @param mcVersion (record component), @param loaderVersion (record component), no description  
  `public record QuiltLoader(String mcVersion, String loaderVersion) implements Loader`
- [ ] L9 `name()`: @return  
  `public String name() { return "QUILT"; }`
- [ ] L12 `iconUrl()`: no description, @return  
  `public String iconUrl()`

## com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/quilt/QuiltLoaderFetcher.java`

- [ ] L18 `QuiltLoaderFetcher`: no description  
  `public class QuiltLoaderFetcher`
- [ ] L25 `getLatestLoaderVersion(String)`: no description, @throws ?  
  `public static String getLatestLoaderVersion(`
- [ ] L119 `getProfileJson(String,String)`: no description, @param minecraftVersion, @param loaderVersion, @return, @throws ?  
  `public static JsonObject getProfileJson(`
- [ ] L138 `getLatestProfile(String)`: no description, @param minecraftVersion, @return, @throws ?  
  `public static JsonObject getLatestProfile(`

## com.dervarex.minified.launch.launch.modding.quilt.QuiltProfileJsonLoader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/quilt/QuiltProfileJsonLoader.java`

- [ ] L10 `QuiltProfileJsonLoader`: no description  
  `public class QuiltProfileJsonLoader`

## com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/vanilla/VanillaLoader.java`

- [ ] L7 `VanillaLoader(String)`: @param mcVersion  
  `public record VanillaLoader(String mcVersion) implements Loader`
- [ ] L7 `VanillaLoader`: @param mcVersion (record component), no description  
  `public record VanillaLoader(String mcVersion) implements Loader`
- [ ] L9 `name()`: @return  
  `public String name() { return "VANILLA"; }`
- [ ] L11 `loaderVersion()`: no description, @return  
  `public String loaderVersion()`
- [ ] L15 `iconUrl()`: no description, @return  
  `public String iconUrl()`

## com.dervarex.minified.launch.launch.modding.vanilla.package-info

`minified-launch/src/main/java/com/dervarex/minified/launch/launch/modding/vanilla/package-info.java`

- [ ] package `com.dervarex.minified.launch.launch.modding.vanilla` has no package description

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

