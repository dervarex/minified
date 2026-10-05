# Documentation

Quick note for transparency: this documentation generator was entirely built with Claude Code.
I don't have the time to keep the docs updated by hand every release, 
nor to deal with all the versioning, so I asked Claude Opus 5.5 to set this up for me
Just wanted to be upfront about that :)

The documentation site is generated from the library sources and the guides in this directory.
API reference, event pages, the event list and the installation snippets come from the code, so they
can't go out of date.

```sh
./gradlew :docs:docs        # writes the site to docs/build/site
./gradlew :docs:docsServe   # builds it and serves it on http://localhost:8000
```

Add `-PdocsStrict=true` to fail on warnings, which is what the `Docs` workflow does before publishing.

## What lives where

| Path                       | Content                                                                                      |
|----------------------------|----------------------------------------------------------------------------------------------|
| `content/index.md`         | Home page                                                                                    |
| `content/guides/**.md`     | Guides, the file path is the url (`guides/minified-launch/basics.md` → `guides/minified-launch/basics/`) |
| `content/events/introduction.md` | Events overview, `{{events}}` inserts the generated list of all events                  |
| `content/events/<Event>.md`| Optional prose for an event page, e.g. "When it fires". Fields come from the event's javadoc   |
| `content/modules/<module>.md` | Optional introduction for a module page in the API reference                              |
| `content/sidebar.txt`      | Left navigation, see the comment at the top of the file                                      |
| `theme/`                   | Stylesheets, script and favicon, copied to the site as they are                              |
| `src/`                     | The generator, a javadoc doclet                                                              |

## Writing guides

Guides are Markdown with front matter (`title`, `description`, optionally `tableOfContents: false` and
`stylesheet: <file in theme/>`). On top of regular Markdown they support what the old Starlight site had:

- raw HTML, including the `field-card`/`fields-grid`/`type-definition` components from `theme/components.css`
- asides: `:::note`, `:::tip`, `:::caution`, `:::danger`, optionally with a title like `:::note[Title]`, closed by `:::`
- `<Tabs>` with `<TabItem label="Java">`, tabs with the same label switch together across the page
- `{{version}}` is replaced with the library version everywhere, also inside code blocks

Inline code that names a documented type or member, like `` `LaunchConfiguration.Builder` `` or
`` `Launcher.launchMinecraft(...)` ``, links to the API reference automatically. When the member doesn't exist
(anymore) the build prints a warning, as it does for broken links, so stale guides show up.

Event descriptions, event fields and enum constants are documented in the javadoc of the event classes. Write
them there instead of in the Markdown so the javadoc jar has them too.

## Versions

The `Docs` workflow publishes every version to its own directory on the `gh-pages` branch (`/3.1.0/`, ...),
keeps `versions.json` for the version picker in the header and redirects the site root to the latest version.
`/latest/<page>` links resolve to the latest version as well. A push to `master` rebuilds the directory of the
version currently set in the root `build.gradle`.
