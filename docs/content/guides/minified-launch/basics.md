---
title: Launching Basics
description: Start your first Minecraft session
---


You should already have a `User` object from the <a href="/guides/minified-auth/authentication" class="link">Authentication Guide</a>.  
If you haven't done that yet, please do so before continuing. <br />
Additionally, you should have the gradle project set up from the <a href="/guides/getting-started" class="link">Getting Started Guide</a>.

## 1. Create the Project

1. Open **IntelliJ IDEA**.
2. Select **New Project** $\rightarrow$ Choose **Gradle** (Java/Kotlin).

---

## 2. Configure Gradle

Open your `build.gradle` file and add the repository and dependency:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    // Replace {{version}} with your preferred version
    implementation 'io.github.dervarex.minified:minified-launch:{{version}}'
}
```

---

## 3. First Startup

<Tabs>
  <TabItem label="Java">

```java
LaunchConfiguration config = new LaunchConfiguration.Builder()
        .downloadThreads(10)
        .launcherName("YourLauncherName")
        .launcherVersion("1.0.0")
        .assetsDirectory(Path.of("<assets-directory>"))
        .librariesDirectory(Path.of("<libraries-directory>"))
        .jarFile(Path.of("<client.jar>"))
        .isDemoUser(false)
        .loader(new VanillaLoader("1.21.11"))
        .build();

Launcher.launchMinecraft(
        user,
        config
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val config = LaunchConfiguration.Builder()
    .downloadThreads(10)
    .launcherName("YourLauncherName")
    .launcherVersion("1.0.0")
    .assetsDirectory(Path.of("<assets-directory>"))
    .librariesDirectory(Path.of("<libraries-directory>"))
    .jarFile(Path.of("<client.jar>"))
    .isDemoUser(false)
    .loader(VanillaLoader("1.21.11"))
    .build()

Launcher.launchMinecraft(
    user,
    config
)
```

  </TabItem>
</Tabs>