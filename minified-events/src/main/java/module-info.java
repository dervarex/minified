/**
 * Events are essential for building your own Launcher.
 * You can receive updates from minified's Backend from them,
 * such as the current launch status, how many assets have
 * been downloaded so far and much more.
 * <br>
 * For example, if you want to show what's currently happening
 * behind the Scenes, there's no way around them.
 */
module minified.minified.events.main {
    requires org.apiguardian.api;
    exports com.dervarex.minified.events.type.connection;
    exports com.dervarex.minified.events;
}