module minified.minified.auth.main {
    exports com.dervarex.minified.auth;

    requires com.google.gson;
    requires org.jetbrains.annotations;

    requires minified.minified.utils.main;
    requires minified.minified.events.main;
    requires minified.minified.java.main;

    requires httpclient;
    requires MinecraftAuth;
}