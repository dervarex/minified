#!/usr/bin/env bash
set -eu

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

DRY=0
[[ "${1:-}" == "--dry" ]] && DRY=1

mapfile -t TARGETS < <(
  {
    find . -maxdepth 3 -type d -name build -not -path '*/.git/*' -print
    find . -type d -name .gradle -not -path '*/.git/*' -print
    find . -type d -name .kotlin -not -path '*/.git/*' -print
    find . -maxdepth 2 -type d \( -name out -o -name bin \) -not -path '*/build/*' -print
  } 2>/dev/null | sort -u
)

if [[ ${#TARGETS[@]} -eq 0 ]]; then
  echo "Nothing to clean, aborting..."
  exit 0
fi

echo "Will remove:"
printf '  %s\n' "${TARGETS[@]}"

if [[ $DRY -eq 1 ]]; then
  echo "(dry run, nothing removed)"
  exit 0
fi

read -r -p "Continue? [y/N] " ans
[[ "$ans" == [yY] ]] || { echo "Aborted."; exit 1; }

for d in "${TARGETS[@]}"; do
  rm -rf -- "$d"
done

echo "Stopping gradle daemons..."
./gradlew --stop >/dev/null 2>&1 || true

echo "Removing old daemon logs..."
find ~/.gradle/daemon -type f -name '*.log' -mtime +7 -delete 2>/dev/null || true

echo "done"