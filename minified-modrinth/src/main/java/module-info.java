module minified.minified.modrinth.main {
    requires java.net.http;
    requires minified.minified.utils.main;
    requires org.apiguardian.api;

    exports com.dervarex.minified.modrinth;
    exports com.dervarex.minified.modrinth.exceptions;
    exports com.dervarex.minified.modrinth.loaders;
    exports com.dervarex.minified.modrinth.projects;
    exports com.dervarex.minified.modrinth.tags;
    exports com.dervarex.minified.modrinth.teams;
    exports com.dervarex.minified.modrinth.users;
    exports com.dervarex.minified.modrinth.versions;
}