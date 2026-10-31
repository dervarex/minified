module minified.minified.worlds.main {
    requires static lombok;
    requires static org.jetbrains.annotations;
    requires transitive minified.minified.utils.main;
    requires org.apiguardian.api;

    exports com.dervarex.minified.worlds.block;
    exports com.dervarex.minified.worlds.chunk;
    exports com.dervarex.minified.worlds.dimension;
    exports com.dervarex.minified.worlds.dimension.data;
    exports com.dervarex.minified.worlds.dimension.region;
    exports com.dervarex.minified.worlds.entity;
    exports com.dervarex.minified.worlds.exceptions;
    exports com.dervarex.minified.worlds.poi;
    exports com.dervarex.minified.worlds.save;
    exports com.dervarex.minified.worlds.save.data;
    exports com.dervarex.minified.worlds.save.level;
    exports com.dervarex.minified.worlds.save.playerdata;
}
