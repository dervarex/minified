module minified.minified.java.main {
    exports com.dervarex.minified.java;
    exports com.dervarex.minified.java.events;
    exports com.dervarex.minified.java.events.download;
    exports com.dervarex.minified.java.events.extract;
    exports com.dervarex.minified.java.exceptions;
    requires java.net.http;
    requires transitive minified.minified.events.main;
    requires minified.minified.utils.main;
    requires transitive org.apiguardian.api;
}