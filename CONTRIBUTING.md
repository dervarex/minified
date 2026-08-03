# Contributing to Minified

Thanks for your interest in contributing to Minified.

This document explains how to set up the project, what kind of contributions are
useful, and what to expect from the review process. Nothing here is meant to be
bureaucratic: if something is unclear, open an issue and ask.

By contributing, you agree that your contributions are licensed under the same
license as this project, which in this case is Apache 2.0.

---

## Table of contents

- [Ways to contribute](#ways-to-contribute)
- [Project structure](#project-structure)
- [Development setup](#development-setup)
- [Building and testing](#building-and-testing)
- [Code style](#code-style)
- [Commit messages](#commit-messages)
- [Branches and pull requests](#branches-and-pull-requests)
- [Reporting bugs](#reporting-bugs)
- [Feature requests and scope](#feature-requests-and-scope)
- [Working with external APIs](#working-with-external-apis)
- [Releases](#releases)

---

## Ways to contribute

- **Bug reports** - reproducible issues with logs are the most valuable thing you
  can send.
- **Bug fixes** - small, focused pull requests are reviewed fastest.
- **New features** - please open an issue first so the design and the scope can be
  discussed before you write unnecessary code.
- **Documentation** - Javadoc on public API, README fixes, usage examples.
- **Testing on other setups** - especially different Minecraft
  versions and other operating systems than Arch Linux.

---

## Project structure

Minified is a multi-module project. Please keep changes inside the module they
belong to and avoid introducing dependencies between modules that do not already
exist.

| Module               | Responsibility                                          |
| -------------------- | -------------------------------------------------------- |
| `minified-auth`      | Account authentication                                   |
| `minified-java`      | Java runtime discovery, download and management          |
| `minified-launch`    | Assembling and starting the game process                 |
| `minified-utils`     | Shared helpers and constants used by the other modules   |
| `minified-modrinth`  | Modrinth integration (mods, modpacks)                    |



Supported mod loaders: Vanilla, Forge, NeoForge, Fabric and Quilt. A change that
touches version or argument handling should be checked against all of them, not
only against the loader you personally use.

---

## Development setup

### Prerequisites

- A JDK (version: see the build script, please match it, do not bump it in a PR
  without discussion)
- Git
- An IDE of your choice. The project is developed with IntelliJ IDEA, so its
  project files and inspections are the closest thing to a reference setup.

### Getting the code

```bash
git clone https://github.com/dervarex/minified.git
cd minified
```

```bash
./gradlew build
```

### Platform support

Linux is the primary target and the platform every change is expected to work on.
Windows and macOS may work(and most likely will), but they are **not officially supported**, and have **not been tested** yet. Pull
requests that improve behaviour there are welcome as long as they do not
complicate the Linux path or add platform-specific hacks to shared code.

---

## Building and testing

```bash
./gradlew build          # compile all modules
./gradlew test           # run the test suite
./gradlew :minified-launch:test   # run tests of a single module
```

Before opening a pull request, please verify that:

- The full build passes.
- New logic is covered by tests where that is reasonably possible. Code that talks
  to remote APIs should be testable without network access - extract the parsing
  and path-building logic so it can be tested on its own.
- No messy debug code, temporary solutions or platform-specific hacks are remaining.

---

## Code style

- Base package is `com.dervarex.minified`; every module keeps its classes under
  its own sub-package.
- Follow the style of the surrounding code. If in doubt, use the IntelliJ default
  Java formatting.
- No wildcard imports.
- Public API needs Javadoc: what the method does, what it throws, and whether it
  performs network or disk I/O.
- Prefer explicit, descriptive names over abbreviations.
- Do not swallow exceptions. Either handle them meaningfully or let them
  propagate with context.
- Keep the library free of UI concerns and of assumptions about a specific
  launcher frontend.
- Avoid adding third-party dependencies. If a change needs one, explain in the
  issue why it cannot reasonably be implemented without it.

---

## Commit messages

Use short, imperative subject lines:

```
fix classpath separator on Linux
handle empty version list from the modrinth API
add endpoint template for Adoptium feature releases
```

Keep unrelated changes in separate commits.

---

## Branches and pull requests

1. Fork the repository and create a branch off the main development branch.
   Suggested naming: `fix/short-description` or `feature/short-description`.
2. Keep the pull request focused on one topic. Large mixed PRs take much longer to
   review and are more likely to be rejected.
3. In the description, explain **what** changed and **why**, and how you tested it
   (Minecraft version, mod loader, OS, Java version).
4. Rebase on the latest main branch before requesting a review, and resolve
   conflicts yourself.
5. Expect review comments. They are about the code, not about you.

Reformatting or renaming across files that are otherwise untouched will not be
merged, it's unnecesary and hides the real changes.

---

## Reporting bugs

Please include:

- Minified version (or commit hash)
- Minecraft version and mod loader (Vanilla / Forge / NeoForge / Fabric / Quilt)
- Operating system and distribution
- Java runtime version used to launch the game, and the one used to run the
  library, if they differ
- What you expected to happen and what happened instead
- Steps to reproduce, as minimal as you can make them
- Relevant logs and stack traces, as text, not as screenshots

If the problem only occurs on Windows or macOS, say so explicitly. Those platforms
are not officially supported yet, so such reports are handled differently.

---

## Feature requests and scope

Minified is a library. It handles downloading, managing and launching, it does
not implement launcher behaviour or user interface logic. Two consequences:

- Features that belong in the launcher on top of the library (for example the ui) are out of scope here.
- CurseForge integration is out of scope. Modrinth is the supported mod platform: it covers most of what's needed, is focused on Minecraft, has a much cleaner API, and is generally considered safer. CurseForge has had a number of malware issues in the past, and popular mods have increasingly been leaving the platform.

Ideas that fit well are things like better crash-report parsing, world/level data
handling, and improvements to instance and runtime management.

If you are unsure whether something belongs in the library or in the application
using it, open an issue and ask before writing code.

---

## Working with external APIs

Endpoint URLs are not scattered through the codebase. URL templates live centrally
in `ApiEndpoints` (package `com.dervarex.minified.utils`). If your change needs a
new remote endpoint, add the template there instead of inlining a URL.

For example, Java runtimes are fetched through the Adoptium v3 API
(`assets/feature_releases`) using templates defined in that class.

When integrating an API:

- Handle non-200 responses and malformed payloads explicitly.
- Do not assume optional fields are present.
- Avoid unnecessary requests; cache where it is safe to do so.
- Respect the rate limits and terms of the service you are calling.

---

## Releases

Releases are cut by the maintainer. Each GitHub release includes sources JARs for
all modules. Contributors do not need to change version numbers in a pull
request, leave versioning to the release process.

---

## Questions

Open an issue with your question. If it turns out to be a common one, it might end up in the README or here.

Don't hesitate to ask, even if you're unsure it's the right place, or if it takes a few tries to get to the actual issue. That's completely normal and not a bother. Everyone makes mistakes and needs to ask things more than once sometimes, that's totally fine. If you'd prefer something quicker and more informal, feel free to just message on Discord directly.