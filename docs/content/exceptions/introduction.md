---
title: Exceptions
description: Every exception Minified throws
---

Minified throws its own exception types, so you can catch errors individually.

## Checked and unchecked

Exceptions marked **checked** extend `Exception` (mostly `IOException`), the compiler makes you catch or declare them.
**Unchecked** ones extend `RuntimeException`, catching them is optional, but it's recommended to catch all exceptions.

Subclasses are indented below their superclass, catching the superclass catches all of them.

## All exceptions

{{exceptions}}
