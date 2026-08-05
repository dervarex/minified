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
}