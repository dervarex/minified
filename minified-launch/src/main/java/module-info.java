/**
 * Downloads, installs and launches Minecraft: Java Edition.
 *
 * <p>The entry point is {@link com.dervarex.minified.launch.launch.Launcher#launchMinecraft Launcher#launchMinecraft}, which takes a
 * logged in {@link com.dervarex.minified.auth.user.User User} (or {@code null} for offline mode) and a
 * {@link com.dervarex.minified.launch.launch.LaunchConfiguration LaunchConfiguration}. Missing assets, libraries and the client jar
 * are downloaded before the game is started</p>
 *
 * <pre>{@code
 * LaunchConfiguration config = new LaunchConfiguration.Builder()
 *         .launcherName("YourLauncherName")
 *         .launcherVersion("1.0.0")
 *         .assetsDirectory(Path.of("<assets-directory>"))
 *         .librariesDirectory(Path.of("<libraries-directory>"))
 *         .jarFile(Path.of("<client.jar>"))
 *         .loader(new VanillaLoader("1.21.11"))
 *         .build();
 *
 * Launcher.launchMinecraft(user, config);
 * }</pre>
 *
 * <h2>Packages</h2>
 * <ul>
 *     <li>{@link com.dervarex.minified.launch.launch} - the launcher and its configuration</li>
 *     <li>{@link com.dervarex.minified.launch.launch.modding} - mod loaders (Vanilla, Fabric, Quilt, Forge,
 *     NeoForge and custom loaders), set through
 *     {@link com.dervarex.minified.launch.launch.LaunchConfiguration.Builder#loader Builder#loader}</li>
 *     <li>{@link com.dervarex.minified.launch.profile} - saving a configuration to disk and loading it later
 *     with {@link com.dervarex.minified.launch.profile.ProfileFactory ProfileFactory}</li>
 *     <li>{@code com.dervarex.minified.launch.events.*} - events posted while downloading, preparing the
 *     environment, installing loaders and launching</li>
 *     <li>{@code com.dervarex.minified.launch.exceptions.*} - exceptions thrown when a launch step fails,
 *     grouped by the step they belong to</li>
 * </ul>
 *
 * <p>This module requires {@code com.dervarex.minified.auth.main} transitively, so the {@code User} type is
 * available with only one implementation</p>
 */
module minified.minified.launch.main {
    requires java.net.http;
    requires java.xml;
    requires static lombok;
    requires transitive minified.minified.auth.main;
    requires minified.minified.events.main;
    requires minified.minified.java.main;
    requires org.jetbrains.annotations;
    requires org.jsoup;
    requires minified.minified.utils.main;
    requires org.apiguardian.api;

    exports com.dervarex.minified.launch.launch;
    exports com.dervarex.minified.launch.launch.modding;
    exports com.dervarex.minified.launch.launch.modding.custom;
    exports com.dervarex.minified.launch.launch.modding.fabric;
    exports com.dervarex.minified.launch.launch.modding.forge;
    exports com.dervarex.minified.launch.launch.modding.forge.api;
    exports com.dervarex.minified.launch.launch.modding.forge.installer;
    exports com.dervarex.minified.launch.launch.modding.neoforge;
    exports com.dervarex.minified.launch.launch.modding.neoforge.api;
    exports com.dervarex.minified.launch.launch.modding.neoforge.installer;
    exports com.dervarex.minified.launch.launch.modding.quilt;
    exports com.dervarex.minified.launch.launch.modding.vanilla;
    exports com.dervarex.minified.launch.profile;
    exports com.dervarex.minified.launch.exceptions.cache;
    exports com.dervarex.minified.launch.exceptions.download;
    exports com.dervarex.minified.launch.exceptions.libraries;
    exports com.dervarex.minified.launch.exceptions.loader;
    exports com.dervarex.minified.launch.exceptions.profile;
    exports com.dervarex.minified.launch.exceptions.version;
    exports com.dervarex.minified.launch.events.download.assets;
    exports com.dervarex.minified.launch.events.download.client;
    exports com.dervarex.minified.launch.events.download.libraries;
    exports com.dervarex.minified.launch.events.environment;
    exports com.dervarex.minified.launch.events.launch;
    exports com.dervarex.minified.launch.events.loader;

}