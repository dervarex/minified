---
title: Quick Start
description: Start from scratch with Minified.
---


## 1. Create the Project

1. Open **IntelliJ IDEA**.
2. Select **New Project** -> **Java/Kotlin** (Build System: Gradle).

---

## 2. Configure Gradle

Open your `build.gradle` file and add the repository and dependency:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    // Replace {{version}} with your preferred version
    implementation 'com.github.dervarex.minified:minified-launch:{{version}}'
}
```
Now, reload the Gradle project to download the dependencies.

Once that finishes, you can start coding your launcher.
