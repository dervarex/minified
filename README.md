# Minified Multi-Module Libraries

Multi-module Gradle project providing small Java utility libraries. Each module builds a Maven-style artifact that can be consumed from Maven or Gradle once published.

## Modules

- `minified-auth` - authentication helpers
- `minified-launch` - launch/runtime helpers
- `minified-java` - Java platform helpers
- `minified-utils` - general utilities

## Build

```bash
./gradlew test
```

If you do not use the Gradle wrapper, run:

```bash
gradle test
```

## Publishing

Each module applies `maven-publish`. You can publish to your local Maven cache for testing:

```bash
./gradlew publishToMavenLocal
```

Then consume with groupId `com.dervarex.minified` and the module artifactId.

