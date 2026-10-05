---
title: Users
description: Fetch Modrinth users.
---


## 1. The Users Client

The `UsersClient` provides access to Modrinth users.

Access it through the main `Modrinth` client:

```java
modrinth.users()
````

## 2. Getting a User

Fetch a user by their ID or username:

<Tabs>
  <TabItem label="Java">

```java
User user = modrinth.users().get("Geometrically");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val user = modrinth.users().get("Geometrically")
```

  </TabItem>
</Tabs>

Both Modrinth user IDs and usernames are supported.

## 3. Getting Multiple Users

Fetch multiple users at once by their IDs:

<Tabs>
  <TabItem label="Java">

```java
List<User> users = modrinth.users().getMany(
        "user-id-1",
        "user-id-2"
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val users = modrinth.users().getMany(
    "user-id-1",
    "user-id-2"
)
```

  </TabItem>
</Tabs>

{/* Two public methods. This page is doing its best. */}
