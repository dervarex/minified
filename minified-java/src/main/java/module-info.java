module minified.minified.java.main {
    exports com.dervarex.minified.java;
    exports com.dervarex.minified.java.events;
    exports com.dervarex.minified.java.events.download;
    exports com.dervarex.minified.java.events.extract;
    requires java.net.http;
    requires minified.minified.events.main;
    requires minified.minified.utils.main;
    requires org.apiguardian.api;
}