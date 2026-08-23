module minified.minified.auth.main {
    exports com.dervarex.minified.auth;
    exports com.dervarex.minified.auth.events;
    exports com.dervarex.minified.auth.exceptions;

    requires com.google.gson;

    requires minified.minified.utils.main;
    requires minified.minified.events.main;
    requires minified.minified.java.main;

    requires httpclient;
    requires MinecraftAuth;

    requires static org.apiguardian.api;
    requires static org.jetbrains.annotations;

    // Allow GSON to reflect data classes
    opens com.dervarex.minified.auth to com.google.gson;
}