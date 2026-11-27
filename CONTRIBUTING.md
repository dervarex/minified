# Contributing to Minified

Thanks for your interest in contributing to Minified.

Here you'll learn, what kind of contributions are useful (most are!)
and what to expect from the review process. If something is unclear, open an issue and ask, it's fine.

By contributing, you agree that your contributions are licensed Apache 2.0.

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

Any contribution is appreciated!

- **Bug reports** - reproducible issues with logs are really nice, they help a lot.
- **Bug fixes** - small, focused pull requests are cool, and you'll officially be a contributor!
- **New features** - please open an issue first so we can discuss it first before you write unnecessary code :)
- **Documentation** - Javadoc on public API, README fixes, usage examples.
- **Testing on other setups** - especially different operating systems than Arch Linux and NixOS.

---

## Project structure

Minified is a multi-module project, so please keep things where they belong.

| Module               | What it does                                          |
| -------------------- | -------------------------------------------------------- |
| `minified-auth`      | Account authentication                                   |
| `minified-java`      | Java runtime discovery, download and management          |
| `minified-launch`    | Assembling and starting the game process                 |
| `minified-utils`     | Shared helpers and constants used by the other modules   |
| `minified-modrinth`  | Modrinth integration (mods, modpacks)                    |


Supported mod loaders: Vanilla, Forge, NeoForge, Fabric and Quilt.
Please test all of them, or ask me to spin up the Version Matrix against your Pull Request, I'll help with fixing Issues.

---

## Development setup

### Prerequisites

- A JDK (version: see the build script)
- Git
- An Code Editor or IDE of your choice. IntelliJ IDEA recommended, but using any other Program capable of editing Files works aswell.

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
Windows and macOS may work(and most likely will), but they are **not officially supported**, and have **not been tested** yet.
You surely can test it on your setups and tell me how it went.
Pull Requests improving behavior for other Operating Systems are great, but please don't use some platform specific hacks or spaghetti code just because it "works".

---

## Building and testing

```bash
./gradlew build                   # compile all modules
./gradlew test                    # run the test suite
./gradlew :minified-launch:test   # run tests of a single module
```

If you open a Pull Request, I can let Actions confirm that:

- The full build passes
- All Tests Pass
- The Version Matrix runs smoothly

What you'll have to confirm yourself:
- New logic is covered by tests where that is reasonably possible. Code that talks
  to remote APIs should be a @Manual test (those are Integration tests if you're wondering, they don't run automatically)
- No messy debug code, temporary solutions or platform-specific hacks are remaining.

---

## Code style

We're not really strict about that, just trying to keep stuff consistent.

- Base package should be `com.dervarex.minified`.
- Follow the style of the surrounding code, IntelliJ IDEA default settings are fine.
- Avoid wildcard imports without a reason
- Public API needs Javadoc.
- Don't call a method doStuff() or idk()
- Do not swallow exceptions.
- Don't do any UI things in this library, seriously, this is a Backend Library, not a design framework :)
- Avoid adding third-party dependencies, we've got most things already in minified-utils, only a good reason justifies a library.
- Public API gets an APIGuardian `@API` status, directly, through its class or
  `package-info.java`. New things should start as `EXPERIMENTAL` and get marked as Stable
  before the PR gets merged, check that with `scripts/api-review.py`, it'll help you a lot.

---

## Commit messages

Use short, imperative subject lines:

```
fix classpath separator on Linux
handle empty version list from the modrinth API
add endpoint template for Adoptium feature releases
```

Inside of PRs, commit as many "fix typo" as you want, it'll get squashed anyways.

---

## Branches and pull requests

1. Fork the repository and create a branch, master is a good starting point
2. Keep the pull request focused on one topic, makes reviewing easier and helps me understand what you tried to do.
3. Tell me what you changed, why you changed it and how you tested it doesn't blow up everything.
4. Try to resolve conflicts yourself, if you're not sure about something, ask me.
5. I'll most likely leave some review comments or ask for changes, that's normal.

Reformatting or renaming across files that are otherwise untouched will not be merged, unless you've got a good reason.

Don't worry about getting everything perfectly right first try, no first Pull Request was perfect

### Merging

(skip this section if you're not a maintainer)

Pull requests or branches are merged with **squash merges**

Since only one commit per branch ends up in the history, its message must clearly describe what changed. Branch names should therefore be descriptive, for example:

```
Add Forum implementation
```

### Branch naming

Branches are usually named after the minified module they focus on, for example, `utils/nbt-parser`.
This doesn't mean a branch is restricted to that module, you can still commit changes to files in other modules within the same branch.

Additionally, branches may have prefixes instead of the module, here are a few good ones:

| Prefix  | Description                                    | Example              |
|---------|------------------------------------------------|----------------------|
| tests/  | JUnit5 Tests for a minified module may be here | tests/api-client     |
| build/  | Gradle-related changes or other scripts        | build/add-dependency |
| docs/   | Documentation changes only                     | docs/readme          |

That doesn't mean that you can't do your own prefixes though, something like `github/add-xyz-workflow` is allowed too, it just has to make sense (`fix/bug` does not make sense)

---

## Reporting bugs

Please include:

- Minified version (or commit hash)
- Minecraft version and mod loader (Vanilla / Forge / NeoForge / Fabric / Quilt) if applicable
- Operating system and distribution
- Java runtime version used to launch the game, and the one used to run the
  library, if they differ
- What you expected to happen and what happened instead
- Steps to reproduce, don't wonder why I might compact it if you write a four page guide.
- Relevant logs and stack traces, as text or screenshots, or encoded as sha256 if you don't want us to know, however you want :) (text is recommended)

If the problem only occurs on Windows or macOS, please tell me, I'll make a VM for testing then.

---

## Feature requests and scope

- Features that belong in the launcher on top of the library (for example the ui) are out of scope here.
- CurseForge integration is out of scope. Modrinth is the supported mod platform: it covers most of what's needed, is focused on Minecraft, has a much cleaner API, and is generally considered safer. CurseForge has had a number of malware issues in the past, and popular mods have increasingly been leaving the platform.

If you are unsure whether your feature fits into minified, open a Issue or Discussion and I'll help you.

---

## Working with external APIs

URL templates live centrally
in `ApiEndpoints` (package `com.dervarex.minified.utils`). If your change needs a
new remote endpoint, add the template there please, it's also great for documenting the endpoints.

For example, Java runtimes are fetched through the Adoptium v3 API
(`assets/feature_releases`) using templates defined in that class.

When integrating an API:

- Expect rate limits :/ (and comply with the service's terms!)
- Handle non-200 responses and weird answers.

Just to mention it here, we've got a great `HttpException`, you can use that one to save a lot of work.

---

## Releases

(skip if you're not a maintainer)

Releases are published to Maven Central as `io.github.dervarex.minified:<module>:<version>`
by the `Release` workflow (`.github/workflows/release.yml`)
Releases may only be done by the maintainer, `scripts/release.sh` helps with it

We need these repository secrets:

- `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` - a user token from the Central Portal
- `SIGNING_KEY` - the private GPG key (`gpg --export-secret-keys --armor <key id>`)
- `SIGNING_KEY_PASSWORD` - its passphrase

(signing hell, I just hope I didn't leak any of those)

`./gradlew publishToMavenLocal` works without any of these, the artifacts are just not signed then, locally that doesn't matter

---

## AI

Tools like Claude Code (and other AI coding assistants) are allowed and partially used in this project.However, I have reviewed every line it generated, that's exactly what you should do aswell, it can generate useful things, but trust me it can mess up everything.

For example, setting up maven central together with claude was really, really hard, for both of us, reading the manual might have been faster.

---
## Questions

Open an issue with your question. If it turns out to be a common one, it might end up in the README or here.

Don't hesitate to ask, even if you're unsure it's the right place, or if it takes a few tries to get to the actual issue. That's completely normal and not a bother. Everyone makes mistakes and needs to ask things more than once sometimes, that's totally fine. If you'd prefer something quicker and more informal, feel free to just message on Discord directly.
