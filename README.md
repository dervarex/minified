# Minified

## Modules

- `minified-auth` - authentication helpers
- `minified-launch` - launch helpers
- `minified-java` - Java download and installation manager
- `minified-utils` - general utilities

## Publishing

Each module applies `maven-publish`.

Publish to Maven Local with:

```bash
./gradlew publishToMavenLocal
```

Then consume with groupId `com.dervarex.minified` and the module artifactId.

todo:
license stuff!