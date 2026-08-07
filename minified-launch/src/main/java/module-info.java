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