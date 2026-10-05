---
title: Intermediary
---
In the Fabric modding ecosystem, Intermediary is a standardized set of mapping names for Minecraft's code.
Because Mojang's raw code changes names with every update, Intermediary provides a stable, consistent naming layer.
This allows Fabric mods to remain compatible across different Minecraft versions without requiring continuous rewrites, as long as the underlying game mechanics haven't changed.

## How Intermediary Works
### Deobfuscation
When Minecraft updates, its obfuscated code (e.g., methods named a, b, c) is processed by the Fabric team's matcher, generating unique, constant names for classes, fields, and methods.
### Independence
Unlike readable mappings (like <a href="./yarn" class="link">Yarn</a>), Intermediary names are usually randomized strings. This neutrality is intentional, preventing name changes from breaking other mods that rely on them.
### The Modding Pipeline
When building a mod, developers write code targeting Intermediary. End user mapping systems (like <a href="./yarn" class="link">Yarn</a> or <a href="../../shared/mojang-mappings" class="link">Mojang Mappings</a>) then translate these stable names into human readable code.