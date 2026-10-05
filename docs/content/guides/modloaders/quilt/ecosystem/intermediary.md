---
title: Intermediary
---
In the Quilt ecosystem, Intermediary is a standardized set of mapping names for Minecraft's code.
Because Mojang's obfuscated names change with every Minecraft release, Intermediary provides a stable naming layer that remains consistent across versions.
This allows Quilt mods to remain compatible across multiple Minecraft versions without constant remapping, provided the underlying game behavior has not significantly changed.


### Deobfuscation

When Minecraft is updated, its obfuscated classes, fields, and methods (such as `a`, `b`, and `c`) are analyzed and assigned stable Intermediary names. These identifiers remain consistent between versions whenever possible.

### Independence

Unlike readable mappings such as <a href="./quilt-mappings" class="link">Quilt Mappings</a>, Intermediary names are intentionally non-descriptive. This stability minimizes breaking changes and provides a reliable target for tooling and compatibility.

### The Modding Pipeline

Mods are distributed using Intermediary names. During development, mapping systems such as <a href="./quilt-mappings" class="link">Quilt Mappings</a> translate these stable identifiers into readable names, allowing developers to work with understandable code while maintaining compatibility across Minecraft versions.
