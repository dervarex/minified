---
title: Authentication
description: Authenticate Minecraft users with Microsoft using Minified Auth.
---


## 1. Add the Dependency

Add `minified-auth` to your `build.gradle` file:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.dervarex.minified:minified-auth:3.0.0'
}
````

---

## 2. Initialize the Auth Manager

Before using authentication, initialize the `AuthManager` with a directory where sessions can be stored.

<Tabs>
  <TabItem label="Java">

```java
Path authDirectory = Path.of("<auth-directory>");

AuthManager.init(authDirectory);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val authDirectory = Path.of("<auth-directory>")

AuthManager.init(authDirectory)
```

  </TabItem>
</Tabs>

---

## 3. Check for a Saved Session

Before starting a new login, check whether a saved session already exists.

<Tabs>
  <TabItem label="Java">

```java
User user = null;

if (AuthManager.hasSessionSaved()) {
    user = AuthManager.loginWithSavedSession();
}
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
var user: User? = null

if (AuthManager.hasSessionSaved()) {
    user = AuthManager.loginWithSavedSession()
}
```

  </TabItem>
</Tabs>

If the saved session is valid, `loginWithSavedSession()` returns the authenticated user.

---

## 4. Start Device Code Login

If no saved session exists, start the Microsoft device code login:

<Tabs>
  <TabItem label="Java">

```java
AuthManager.startDeviceCodeLoginAsync();
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
AuthManager.startDeviceCodeLoginAsync()
```

  </TabItem>
</Tabs>

:::tip[Blocking alternative]
`AuthManager.login()` runs the same device-code flow synchronously and returns the `User` directly instead of updating `LoginState` in the background - but it also throws `LoginFailedException` on failure rather than reporting it through `LoginState.status`. Only use it from a dedicated background thread; calling it from a UI thread blocks until the user finishes authorizing in the browser. `startDeviceCodeLoginAsync()` (shown above) is the recommended entry point for most launchers.
:::

The login runs asynchronously. You can access its current state using:

<Tabs>
  <TabItem label="Java">

```java
LoginState state = AuthManager.getLoginState();
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val state = AuthManager.getLoginState()
```

  </TabItem>
</Tabs>

Wait until the user code becomes available:

<Tabs>
  <TabItem label="Java">

```java
while (true) {
    LoginState state = AuthManager.getLoginState();

    if (state.userCode != null && state.verificationUri != null) {
        System.out.println(
                "Go to " + state.verificationUri
                + " and enter code " + state.userCode
        );
        break;
    }

    Thread.sleep(500);
}
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
while (true) {
    val state = AuthManager.getLoginState()

    if (state.userCode != null && state.verificationUri != null) {
        println(
            "Go to ${state.verificationUri} and enter code ${state.userCode}"
        )
        break
    }

    Thread.sleep(500)
}
```

  </TabItem>
</Tabs>

The user can then open the verification URL and enter the displayed code.

---

## 5. Wait for Authentication

After displaying the login instructions, wait until authentication succeeds or fails:

<Tabs>
  <TabItem label="Java">

```java
while (true) {
    LoginState state = AuthManager.getLoginState();

    if (state.status == AuthManager.LoginStatus.SUCCESS) {
        break;
    }

    if (state.status == AuthManager.LoginStatus.ERROR) {
        throw new IllegalStateException(state.message);
    }

    Thread.sleep(500);
}
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
while (true) {
    val state = AuthManager.getLoginState()

    if (state.status == AuthManager.LoginStatus.SUCCESS) {
        break
    }

    if (state.status == AuthManager.LoginStatus.ERROR) {
        throw IllegalStateException(state.message)
    }

    Thread.sleep(500)
}
```

  </TabItem>
</Tabs>

After a successful login, get the authenticated user:

<Tabs>
  <TabItem label="Java">

```java
User user = AuthManager.getUser();

System.out.println(
        "Logged in as "
        + user.username()
        + " ("
        + user.uuid()
        + ")"
);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val user = AuthManager.getUser()

println(
    "Logged in as ${user.username} (${user.uuid})"
)
```

  </TabItem>
</Tabs>

---

## 6. Listening for State Changes

Polling `getLoginState()` in a loop works, but `AuthManager` also supports pushing state changes directly to a listener - useful if your UI framework already has a callback/observer pattern instead of a poll loop:

<Tabs>
  <TabItem label="Java">

```java
LoginStateChangeListener listener = newState -> {
    System.out.println("New login state: " + newState.status);
};

AuthManager.addStateChangeListener(listener);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val listener = LoginStateChangeListener { newState ->
    println("New login state: ${newState.status}")
}

AuthManager.addStateChangeListener(listener)
```

  </TabItem>
</Tabs>

The listener is invoked with the same `LoginState` snapshot every time `AuthManager` updates it - during `login()`, `startDeviceCodeLoginAsync()`, and `resetLoginState()`. Remove it once you no longer need updates (e.g. when the login UI closes), the same way you would unsubscribe from any other event in Minified:

<Tabs>
  <TabItem label="Java">

```java
AuthManager.removeStateChangeListener(listener);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
AuthManager.removeStateChangeListener(listener)
```

  </TabItem>
</Tabs>

---

## Complete Example

<Tabs>
  <TabItem label="Java">

```java
Path authDirectory = Path.of("<auth-directory>");

AuthManager.init(authDirectory);

User user = null;

if (AuthManager.hasSessionSaved()) {
    user = AuthManager.loginWithSavedSession();
}

if (user == null) {
    AuthManager.startDeviceCodeLoginAsync();

    boolean loginInstructionsShown = false;

    while (true) {
        LoginState state = AuthManager.getLoginState();

        if (!loginInstructionsShown
                && state.userCode != null
                && state.verificationUri != null) {

            System.out.println(
                    "Go to " + state.verificationUri
                    + " and enter code " + state.userCode
            );

            loginInstructionsShown = true;
        }

        if (state.status == AuthManager.LoginStatus.SUCCESS) {
            user = AuthManager.getUser();
            break;
        }

        if (state.status == AuthManager.LoginStatus.ERROR) {
            throw new IllegalStateException(state.message);
        }

        Thread.sleep(500);
    }
}

System.out.println(
        "Logged in as "
        + user.username()
        + " ("
        + user.uuid()
        + ")"
);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val authDirectory = Path.of("<auth-directory>")

AuthManager.init(authDirectory)

var user: User? = null

if (AuthManager.hasSessionSaved()) {
    user = AuthManager.loginWithSavedSession()
}

if (user == null) {
    AuthManager.startDeviceCodeLoginAsync()

    var loginInstructionsShown = false

    while (true) {
        val state = AuthManager.getLoginState()

        if (!loginInstructionsShown
            && state.userCode != null
            && state.verificationUri != null
        ) {
            println(
                "Go to ${state.verificationUri} and enter code ${state.userCode}"
            )

            loginInstructionsShown = true
        }

        if (state.status == AuthManager.LoginStatus.SUCCESS) {
            user = AuthManager.getUser()
            break
        }

        if (state.status == AuthManager.LoginStatus.ERROR) {
            throw IllegalStateException(state.message)
        }

        Thread.sleep(500)
    }
}

println(
    "Logged in as ${user!!.username} (${user.uuid})"
)
```

  </TabItem>
</Tabs>

The returned `User` can now be passed directly to Minified Launch:

<Tabs>
  <TabItem label="Java">

```java
Launcher.launchMinecraft(
        user,
        config
);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
Launcher.launchMinecraft(
    user,
    config
)
```

  </TabItem>
</Tabs>

---

## Encryption Events

Sessions are stored encrypted on disk using a master key. `AuthManager.init()` accepts an `EventBus` that these events are dispatched on, see <a href="/events/auth/createmasterkeyevent" class="link">`CreateMasterKeyEvent`</a>, <a href="/events/auth/saveencryptedsessionevent" class="link">`SaveEncryptedSessionEvent`</a> and <a href="/events/auth/loadencryptedsessionevent" class="link">`LoadEncryptedSessionEvent`</a> in the <a href="/events/introduction" class="link">Events section</a> for details on exactly when each one fires.
