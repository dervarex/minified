#!/usr/bin/env bash
# Publishes the engine
#
#   docs/publish-engine.sh <engine dir> <gh-pages dir>
set -euo pipefail

ENGINE="$1"
PAGES="$2"

mkdir -p "$PAGES/assets"
NAMES=()
for FILE in "$ENGINE"/*; do
  NAME="$(basename "$FILE")"
  if [ "$NAME" = favicon.svg ]; then
    cp "$FILE" "$PAGES/favicon.svg"
  else
    cp "$FILE" "$PAGES/assets/$NAME"
    NAMES+=("${NAME//./\\.}")
  fi
done
PATTERN="$(IFS='|'; echo "${NAMES[*]}")"

for DIR in "$PAGES"/*/; do
  DIR="${DIR%/}"
  [ "$DIR" != "$PAGES/assets" ] && [ -f "$DIR/assets/site.js" ] || continue
  find "$DIR" -name '*.html' -exec sed -i -E \
    -e "s#(href|src)=\"([./]*)assets/($PATTERN)\"#\\1=\"\\2../assets/\\3\"#g" \
    -e "s#href=\"([./]*)favicon\\.svg\"#href=\"\\1../favicon.svg\"#g" {} +
  for FILE in "$ENGINE"/*; do
    rm -f "$DIR/assets/$(basename "$FILE")"
  done
  rm -f "$DIR/favicon.svg"
  echo "$(basename "$DIR") now uses the shared engine"
done
