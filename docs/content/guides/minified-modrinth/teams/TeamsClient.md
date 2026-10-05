---
title: Teams
description: Fetch Modrinth teams and their members.
---


## 1. The Teams Client

The `TeamsClient` provides access to Modrinth teams and their members.

Access it through the main `Modrinth` client:

```java
modrinth.teams()
````

## 2. Getting a Team

Fetch a team by its ID:

<Tabs>
  <TabItem label="Java">

```java
Team team = modrinth.teams().get("team-id");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val team = modrinth.teams().get("team-id")
```

  </TabItem>
</Tabs>

## 3. Getting Multiple Teams

Fetch multiple teams at once:

<Tabs>
  <TabItem label="Java">

```java
List<Team> teams = modrinth.teams().getMany(
        "team-id-1",
        "team-id-2"
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val teams = modrinth.teams().getMany(
    "team-id-1",
    "team-id-2"
)
```

  </TabItem>
</Tabs>

## 4. Getting Team Members

Fetch all members of a team:

<Tabs>
  <TabItem label="Java">

```java
List<TeamMember> members = modrinth.teams().members("team-id");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val members = modrinth.teams().members("team-id")
```

  </TabItem>
</Tabs>
