#!/usr/bin/env bash
# Releases a new version
#
#   scripts/release.sh                 opens the setup screen to pick the version and options
#   scripts/release.sh <x.y.z | major | minor | patch> [--dry] [--skip-tests] [--no-watch]
#
# Requires: bash, git, gh (logged in), gum, python3, GNU sed/sort
#
# Disclaimer: this script is written by Claude under my direction, it works as intended, just to be clear about that
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

BRANCH=master
WORKFLOW=release.yml

usage() { sed -n '2,11p' "$0" | sed 's/^# \{0,1\}//'; }

DRY=0 TESTS=1 WATCH=1 TARGET=""
for arg in "$@"; do
  case "$arg" in
    --dry) DRY=1 ;;
    --skip-tests) TESTS=0 ;;
    --no-watch) WATCH=0 ;;
    -h|--help) usage; exit 0 ;;
    -*) echo "unknown option $arg" >&2; exit 2 ;;
    *) [[ -n "$TARGET" ]] && { echo "only one version please" >&2; exit 2; }; TARGET="$arg" ;;
  esac
done

command -v gum >/dev/null || { echo "gum is not installed (https://github.com/charmbracelet/gum)" >&2; exit 1; }

# output helpers
step() { echo; gum style --bold "==> $*"; }
info() { gum style --faint "    $*"; }
ok()   { if [[ $DRY -eq 1 ]]; then gum style --faint "    (dry) $*"; else gum style --foreground 2 "    ✓ $*"; fi; }
fail() { gum style --foreground 1 --bold "error: $*" >&2; exit 1; }


UNDO=()
undo() {
  [[ ${#UNDO[@]} -eq 0 ]] && { info "nothing to undo"; return; }
  for (( i=${#UNDO[@]}-1; i>=0; i-- )); do eval "${UNDO[i]}"; done
  ok "undid the local changes, nothing was pushed"
}

# versions
CURRENT="$(sed -n "s/^\s*version = '\([^']*\)'.*/\1/p" build.gradle | head -n1)"
[[ "$CURRENT" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || fail "can't read the version from build.gradle (got '$CURRENT')"
LATEST_TAG="$(git tag --list 'v[0-9]*' | sed 's/^v//' | sort -V | tail -n1)"
LATEST_TAG="${LATEST_TAG:-0.0.0}"
HIGHEST="$(printf '%s\n%s\n' "$CURRENT" "$LATEST_TAG" | sort -V | tail -n1)"

newer() { [[ "$1" != "$2" && "$(printf '%s\n%s\n' "$1" "$2" | sort -V | tail -n1)" == "$1" ]]; }

# checks
step "Checking the repo"
command -v gh >/dev/null || fail "gh is not installed"
gh auth status >/dev/null 2>&1 || fail "gh is not logged in, run: gh auth login"
[[ "$(git rev-parse --abbrev-ref HEAD)" == "$BRANCH" ]] || fail "not on $BRANCH, run: git checkout $BRANCH"
{ git diff --quiet && git diff --cached --quiet; } || fail "uncommitted changes, commit or stash them first"
UPSTREAM="$(git rev-parse --abbrev-ref "$BRANCH@{upstream}" 2>/dev/null)" || fail "$BRANCH has no upstream"
REMOTE="${UPSTREAM%%/*}"
gum spin --title "Fetching $REMOTE" -- git fetch --quiet --tags "$REMOTE"
[[ "$(git rev-parse HEAD)" == "$(git rev-parse "$UPSTREAM")" ]] || fail "$BRANCH differs from $UPSTREAM, pull or push first"
ok "on $BRANCH, clean, up to date with $UPSTREAM"
info "build.gradle $CURRENT, latest tag v$LATEST_TAG, $(git rev-list --count "v$LATEST_TAG..HEAD" 2>/dev/null || echo '?') commits since"

# pick the version
if [[ -z "$TARGET" ]]; then
  command -v python3 >/dev/null || fail "python3 is needed for the setup screen, or pass a version"
  CHOICE="$(mktemp)"
  trap 'rm -f "$CHOICE"' EXIT
  python3 "$ROOT/scripts/release-tui.py" "$CURRENT" "$HIGHEST" "$CHOICE" || { gum style --foreground 3 "Cancelled."; exit 1; }
  read -r VERSION TESTS WATCH DRY_PICK <"$CHOICE"
  [[ "$DRY_PICK" == 1 ]] && DRY=1
else
  IFS=. read -r MAJOR MINOR PATCH <<<"$HIGHEST"
  case "$TARGET" in
    major) VERSION="$((MAJOR + 1)).0.0" ;;
    minor) VERSION="$MAJOR.$((MINOR + 1)).0" ;;
    patch) VERSION="$MAJOR.$MINOR.$((PATCH + 1))" ;;
    *) VERSION="${TARGET#v}" ;;
  esac
fi
[[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || fail "'$VERSION' is no version like 1.2.3"
newer "$VERSION" "$HIGHEST" || fail "$VERSION is not newer than $HIGHEST"
TAG="v$VERSION"
git rev-parse -q --verify "refs/tags/$TAG" >/dev/null && fail "tag $TAG already exists locally"
git ls-remote --exit-code --tags "$REMOTE" "refs/tags/$TAG" >/dev/null 2>&1 && fail "tag $TAG already exists on $REMOTE"

# plan
yesno() { [[ "$1" == 1 ]] && echo yes || echo no; }
echo
gum style --border rounded --padding "0 2" --margin "0 2" \
  "$(gum style --bold "Release $CURRENT -> $VERSION")" \
  "" \
  "tag            $TAG" \
  "push to        $REMOTE ($(git remote get-url "$REMOTE"))" \
  "local tests    $(yesno "$TESTS")" \
  "watch workflow $(yesno "$WATCH")" \
  "dry run        $(yesno "$DRY")" \
  "" \
  "$(gum style --bold "Steps")" \
  "$([[ $TESTS -eq 1 ]] && echo "• run the tests locally" || echo "• skip the local tests")" \
  "• set the version in build.gradle and the README to $VERSION" \
  "• commit it as \"Release $TAG\" and tag it $TAG" \
  "• push $BRANCH and $TAG to $REMOTE, which publishes to Maven Central" \
  "$([[ $WATCH -eq 1 ]] && echo "• watch the Release workflow" || echo "• don't watch the workflow")"
echo
if [[ $DRY -eq 1 ]]; then
  info "dry run: the steps only say what they would do"
else
  gum style --foreground 3 "  The push can't be undone, a Maven Central release can't be changed or deleted."
  gum confirm --default=false "Release $TAG?" || { gum style --foreground 3 "Cancelled, nothing changed."; exit 1; }
fi

# tests
if [[ $TESTS -eq 1 ]]; then
  step "Tests"
  WORKERS=$(( $(nproc) - 2 )); (( WORKERS < 1 )) && WORKERS=1
  if [[ $DRY -eq 0 ]]; then
    gum spin --show-error --title "Running the tests, this takes a while" -- nice ./gradlew test --max-workers="$WORKERS" \
      || fail "tests failed, nothing was changed"
  fi
  ok "tests passed"
fi

# bump, commit, tag
step "Version"
if [[ $DRY -eq 0 ]]; then
  sed -i "s/^\(\s*version = \)'$CURRENT'/\1'$VERSION'/" build.gradle
  UNDO+=("git checkout --quiet -- build.gradle")
  grep -q "version = '$VERSION'" build.gradle || { undo; fail "bumping build.gradle failed"; }
  sed -i "s/\(io\.github\.dervarex\.minified:minified-launch:\)[0-9][0-9.]*/\1$VERSION/" README.md
  UNDO+=("git checkout --quiet -- README.md")
  grep -q "minified-launch:$VERSION'" README.md || { undo; fail "bumping the README failed"; }
fi
ok "build.gradle and the README are $VERSION"

if [[ $DRY -eq 0 ]]; then
  git commit --quiet -m "Release $TAG" -- build.gradle README.md
  UNDO=("git reset --quiet --keep HEAD~1")
fi
ok "committed $(git rev-parse --short HEAD)"

if [[ $DRY -eq 0 ]]; then
  git tag -a "$TAG" -m "Minified $TAG"
  UNDO+=("git tag -d '$TAG' >/dev/null")
fi
ok "tagged $TAG"

# push
step "Push"
if [[ $DRY -eq 0 ]]; then
  gum spin --show-error --title "Pushing" -- git push --atomic "$REMOTE" "$BRANCH" "$TAG" || { undo; fail "push failed"; }
fi
UNDO=()
ok "pushed"

if [[ $WATCH -eq 0 || $DRY -eq 1 ]]; then
  echo
  info "follow it with: gh run list --workflow $WORKFLOW"
  exit 0
fi

# watch
step "Release workflow"
RUN_ID=""
for _ in $(seq 1 30); do
  RUN_ID="$(gh run list --workflow "$WORKFLOW" --branch "$TAG" --limit 1 --json databaseId -q '.[0].databaseId' 2>/dev/null || true)"
  [[ -n "$RUN_ID" ]] && break
  sleep 2
done
[[ -n "$RUN_ID" ]] || fail "the workflow didn't start, check: gh run list --workflow $WORKFLOW"

if ! gh run watch "$RUN_ID" --exit-status; then
  echo
  gum style --foreground 1 "The workflow failed. Nothing reaches Maven Central when the tests fail."
  info "rerun the failed jobs with: gh run rerun $RUN_ID --failed"
  exit 1
fi

step "Released $TAG"
info "Maven Central can take up to ~30 min until io.github.dervarex.minified:*:$VERSION shows up."
info "The GitHub release is a draft, write the intro and publish it:"
info "$(gh release view "$TAG" --json url -q .url 2>/dev/null || echo "gh release view $TAG --web")"
info "or open it with: gh release view $TAG --web"
