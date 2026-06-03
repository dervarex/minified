# Minified

Java API for Minecraft version management, asset downloading, library resolution and game launching.

## Features

* Version management
* Asset downloading
* Library downloading
* Native extraction
* Minecraft launching
* Microsoft account authentication
* Session management

## Usage

### JitPack

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.dervarex.minified:minified-launch:v1.1.0'
}
```

### Available modules

- `minified-auth`
- `minified-java`
- `minified-launch`
- `minified-utils`

## Documentation

Documentation is available in the Javadoc.

## Testing

The project currently contains mostly functional and integration tests.

These tests are designed to verify that the API works correctly in real-world scenarios, including authentication, downloading game files, resolving libraries, extracting natives and launching Minecraft.

The existing tests are not intended to serve as examples of best practices or recommended API usage. They are primarily used to validate functionality during development.

One of the integration tests performs a complete launch workflow, including:

* Microsoft device code authentication
* Session restoration
* User authentication
* Asset downloading
* Library downloading
* Native extraction
* Minecraft launching

More unit tests and dedicated usage examples are planned in the future.

## Roadmap

* Modding support
* Additional unit tests
* More usage examples
* Improved platform-specific testing

## License

Licensed under the Apache License 2.0.

See the LICENSE file for details.
