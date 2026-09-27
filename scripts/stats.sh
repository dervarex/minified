#!/usr/bin/env bash

# Requires: bash 4+, GNU coreutils, git (optional, for the hotspot detection), fzf (for the menu)
# For the Windows users: use Git Bash or WSL
# Most of the utils (especially awk for working with text) are written by deepseek v4.1 flash (probably), they surely work better than what I could've done (absolutely not first try though)
export LC_ALL=C
set -eu # safety mode

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)" # minified root folder
cd "$ROOT"

CACHE="$(mktemp -d)"
trap 'rm -rf "$CACHE"' EXIT

# 24-bit truecolor if stdout is a TTY and NO_COLOR isn't set
if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
  C_RESET=$'\033[0m'; C_DIM=$'\033[2m'; C_BOLD=$'\033[1m'
  C_HEAD=$'\033[38;2;180;140;255m'
  C_ACCENT=$'\033[38;2;120;180;255m'
  C_OK=$'\033[38;2;120;200;130m'
  C_WARN=$'\033[38;2;230;180;80m'
  C_BAD=$'\033[38;2;230;100;100m'
  C_MUTED=$'\033[38;2;140;140;150m'
  C_NUM=$'\033[38;2;220;220;230m'
else
  C_RESET= C_DIM= C_BOLD= C_HEAD= C_ACCENT= C_OK= C_WARN= C_BAD= C_MUTED= C_NUM=
fi


# Utils

hr() {
  local w="${COLUMNS:-100}"
  printf '%s' "$C_MUTED"; printf '─%.0s' $(seq 1 "$w"); printf '%s\n' "$C_RESET"
}

section() {
  local title="$1"
  local w="${COLUMNS:-100}"
  local prefix="${title} "
  local fill=$(( w - ${#prefix} - 1 ))
  [[ $fill -lt 0 ]] && fill=0
  printf '\n%s%s%s' "$C_HEAD" "$prefix" "$C_RESET"
  printf '%s' "$C_MUTED"; printf '─%.0s' $(seq 1 "$fill"); printf '%s\n' "$C_RESET"
}

sub() {
  printf '\n  %sׂ╰┈➤ %s%s\n' "$C_ACCENT" "$1" "$C_RESET" # cool symbol :)
}

fmt_num() {
  local n="$1"
  if command -v numfmt >/dev/null 2>&1; then numfmt --grouping "$n" 2>/dev/null || echo "$n"
  else echo "$n"; fi
}

pct() {
  awk -v a="$1" -v b="$2" 'BEGIN{ if (b==0) print "0.0"; else printf "%.1f", 100*a/b }';
}

bar() {
  local value="$1" max="$2" width="${3:-24}"
  [[ "$max" -eq 0 ]] && max=1
  local n=$(( value * width / max ))
  [[ $n -lt 1 && $value -gt 0 ]] && n=1
  local i
  for ((i=0;i<n;i++)); do printf '█'; done
  for ((i=n;i<width;i++)); do printf '·'; done
}

color_for_pct() {
  awk -v p="$1" 'BEGIN{ if (p>=80) print "ok"; else if (p>=50) print "warn"; else print "bad" }'
}

# Utils end

declare -a MODULES=()

detect_modules() {
  local settings=""
  [[ -f "$ROOT/settings.gradle.kts" ]] && settings="$ROOT/settings.gradle.kts"
  [[ -z "$settings" && -f "$ROOT/settings.gradle" ]] && settings="$ROOT/settings.gradle"
  if [[ -z "$settings" ]]; then
    echo "${C_BAD}Kein settings.gradle(.kts) gefunden.${C_RESET}" >&2
    return 1
  fi
  mapfile -t MODULES < <(
    grep -oE "include[[:space:]]*\(?[[:space:]]*['\"][^'\"]+['\"]" "$settings" 2>/dev/null \
      | sed -E "s/include[[:space:]]*\(?[[:space:]]*['\"]//; s/['\"]$//" \
      | tr ':' '/' \
      | sed 's|^/||' \
      | sort -u
  )
  if [[ ${#MODULES[@]} -eq 0 ]]; then
    # Fallback, this shouldn't be needed though
    mapfile -t MODULES < <(
      find "$ROOT" -type d -path '*/src/main/java' -not -path '*/build/*' 2>/dev/null \
        | sed "s|$ROOT/||; s|/src/main/java||" | sort -u
    )
  fi
}

module_of_path() {
  local p="$1" m best="" bestlen=0
  for m in "${MODULES[@]}"; do
    if [[ "$p" == "$ROOT/$m/"* || "$p" == "$m/"* ]]; then
      if [[ ${#m} -gt $bestlen ]]; then best="$m"; bestlen=${#m}; fi
    fi
  done
  [[ -z "$best" ]] && best="(root)"
  echo "$best"
}

# File records (awk)

# back to the 1977
write_records_awk() {
  cat > "$CACHE/records.awk" <<'AWK'
BEGIN {
  split("if for while switch catch do else try synchronized return new assert throw", _c, " ")
  for (_i in _c) ctrl[_c[_i]] = 1
  while ((getline line < ignorefile) > 0) {
    sub(/\r$/, "", line)
    if (line != "" && line !~ /^#/) pats[++np] = line
  }
}
FNR == 1 {
  if (NR > 1) emit()
  file = FILENAME
  skip = 0
  base = file; sub(/.*\//, "", base); sub(/\.java$/, "", base)
  for (i=1; i<=np; i++) if (base ~ pats[i] || file ~ pats[i]) { skip = 1; break }
  if (skip) { file = ""; next }
  loc = code = cmt = blank = 0
  cls = ifc = enu = rec = ann = 0
  meth = 0
  depth = 0; maxd = 0
  inblock = 0
}
skip { next }
{
  loc++
  t = $0
  gsub(/\r$/, "", t)
  sub(/^[ \t]+/, "", t)
  sub(/[ \t]+$/, "", t)

  if (inblock) { cmt++; if (index(t, "*/") > 0) inblock = 0; next }
  if (t == "") { blank++; next }
  if (t ~ /^\/\//) { cmt++; next }
  if (t ~ /^\/\*/) { cmt++; if (index(t, "*/") == 0) inblock = 1; next }

  code++

  if (t ~ /(^|[^A-Za-z0-9_])class[ \t]+[A-Za-z_]/) cls++
  if (t ~ /(^|[^A-Za-z0-9_])interface[ \t]+[A-Za-z_]/ && t !~ /@interface/) ifc++
  if (t ~ /@interface[ \t]+[A-Za-z_]/) ann++
  if (t ~ /(^|[^A-Za-z0-9_])enum[ \t]+[A-Za-z_]/) enu++
  if (t ~ /(^|[^A-Za-z0-9_])record[ \t]+[A-Za-z_]/) rec++

  if (t !~ /^@/ && t ~ /\(/ && t ~ /\)/) {
    first = t
    sub(/[ \t(].*/, "", first)
    if (!(first in ctrl) && t !~ /;[ \t]*$/) {
      if (t ~ /[)][ \t]*(\{|throws|$)/ || t ~ /\{[ \t]*$/) meth++
    }
  }

  d = t; depth += gsub(/\{/, "", d)
  d = t; depth -= gsub(/\}/, "", d)
  if (depth < 0) depth = 0
  if (depth > maxd) maxd = depth
}
END { emit() }
function emit() {
  if (file == "") return
  printf "%d\t%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\n",
    loc, file, code, cmt, blank, cls, ifc, enu, rec, ann, meth, maxd
}
AWK
}

build_records() {
  write_records_awk
  local ignore="$ROOT/scripts/ignore"
  [[ -f "$ignore" ]] || ignore=/dev/null

  find "$ROOT" -type f -name '*.java' \
    -not -path '*/build/*' -not -path '*/.gradle/*' -not -path '*/.git/*' -print0 \
  | xargs -0 -r awk -v ignorefile="$ignore" -f "$CACHE/records.awk" > "$CACHE/raw.tsv"

  awk -F'\t' -v root="$ROOT" -v mods="$(IFS=,; echo "${MODULES[*]}")" '
  BEGIN { n = split(mods, M, ",") }
  {
    path = $2
    sub("^" root "/", "", path)
    best = "(root)"; bestlen = 0
    for (i=1; i<=n; i++) {
      m = M[i]
      if (index(path, m "/") == 1 && length(m) > bestlen) { best = m; bestlen = length(m) }
    }
    print best "\t" $0
  }' "$CACHE/raw.tsv" > "$CACHE/records.tsv"
}

build_extras() {
  local files_fifo
  # Imports
  find "$ROOT" -type f -name '*.java' \
    -not -path '*/build/*' -not -path '*/.gradle/*' -print0 \
  | xargs -0 -r grep -hE '^[[:space:]]*import[[:space:]]+' 2>/dev/null \
  | sed -E 's/^[[:space:]]*import[[:space:]]+(static[[:space:]]+)?//; s/;[[:space:]]*$//' \
  | sort | uniq -c | sort -rn > "$CACHE/imports.txt" || true

  # Annotations
  find "$ROOT" -type f -name '*.java' \
    -not -path '*/build/*' -not -path '*/.gradle/*' -print0 \
  | xargs -0 -r grep -ohE '@[A-Z][A-Za-z0-9_]*' 2>/dev/null \
  | sort | uniq -c | sort -rn > "$CACHE/annotations.txt" || true

  # TODOs
  find "$ROOT" -type f -name '*.java' \
    -not -path '*/build/*' -not -path '*/.gradle/*' -print0 \
  | xargs -0 -r grep -HniEw '(TODO|FIXME|XXX|HACK)' 2>/dev/null > "$CACHE/todos.txt" || true

  # Smells
  find "$ROOT" -type f -name '*.java' \
    -not -path '*/build/*' -not -path '*/.gradle/*' -print0 \
  | xargs -0 -r grep -HnE \
    -e 'System\.out\.println' \
    -e '\.printStackTrace\(' \
    -e 'catch[[:space:]]*\([[:space:]]*Exception[[:space:]]+[A-Za-z_]+[[:space:]]*\)[[:space:]]*\{[[:space:]]*\}' \
    2>/dev/null > "$CACHE/smells.txt" || true

  # JaCoCo XMLs
  find "$ROOT" -type f \( -name 'jacoco*.xml' -o -name '*TestReport.xml' \) \
    -path '*/build/reports/jacoco/*' -not -path '*/html/*' 2>/dev/null \
    | sort > "$CACHE/jacoco.txt" || true

  # hotspots (git, last 6 months)
  if git -C "$ROOT" rev-parse --git-dir >/dev/null 2>&1; then
    git -C "$ROOT" log --since='6 months ago' --name-only --pretty=format: \
      -- '*.java' 2>/dev/null | grep -v '^$' | sort | uniq -c | sort -rn \
      > "$CACHE/hotspots.txt" || true
  fi

  # File hygiene
  find "$ROOT" -type f -name '*.java' -not -path '*/build/*' -not -path '*/.gradle/*' -print0 \
  | xargs -0 -r grep -lU $'\r' 2>/dev/null > "$CACHE/crlf.txt" || true
}

build_cache() {
  printf '%sBuilding cache...%s\r' "$C_MUTED" "$C_RESET"
  build_records
  build_extras
  printf '%*s\r' 40 ''
}

# Sections

sec_modules() {
  section "Module Overview"
  printf '  %s%-30s %10s %8s %10s%s\n' "$C_MUTED" "module" "LOC" "files" "test-LOC" "$C_RESET"
  hr
  awk -F'\t' -v root="$ROOT" '
  {
    mod = $1; loc = $2; file = $3
    isTest = (file ~ "/src/test/")
    total[mod]  += loc
    files[mod]  += 1
    if (isTest) tloc[mod] += loc
  }
  END {
    for (m in total) printf "%s\t%d\t%d\t%d\n", m, total[m], files[m], tloc[m]
  }' "$CACHE/records.tsv" \
  | sort -t$'\t' -k2,2nr \
  | awk -F'\t' '{ printf "  %-30s %10d %8d %10d\n", $1, $2, $3, $4 }'

  # Totals
  local t_total t_files t_test
  t_total=$(awk -F'\t' '{s+=$2} END{print s+0}' "$CACHE/records.tsv")
  t_files=$(wc -l < "$CACHE/records.tsv" | tr -d ' ')
  t_test=$(awk -F'\t' '$3 ~ /\/src\/test\// {s+=$2} END{print s+0}' "$CACHE/records.tsv")
  hr
  printf '  %s%-30s %10s %8s %10s%s\n' "$C_BOLD" "TOTAL" \
    "$(fmt_num "$t_total")" "$(fmt_num "$t_files")" "$(fmt_num "$t_test")" "$C_RESET"
}

sec_module_detail() {
  local mod="${1:-}"
  section "Module Detail"
  if [[ -z "$mod" ]]; then
    if command -v fzf >/dev/null 2>&1; then
      mod=$(printf '%s\n' "${MODULES[@]}" | fzf \
        --prompt='module> ' \
        --height=~40% \
        --reverse \
        --border=rounded \
        --header="esc: back" \
        --color='fg:250,fg+:#75b4ff,hl:#75b4ff,hl+:#b48cff,prompt:#b48cff,pointer:#e6b450') || {
        return
      }
      [[ -z "$mod" ]] && return
      clear
      section "Module Detail"
    else
      printf '  %sPick a module:%s\n' "$C_MUTED" "$C_RESET"
      printf '%s\n' "${MODULES[@]}" | sed 's/^/  /'
      printf '\n  Usage: %sstats.sh module <name>%s\n' "$C_ACCENT" "$C_RESET"
      return
    fi
  fi
  local total files classes ifc enu rec ann meth
  total=$(awk -F'\t' -v m="$mod" '$1==m {s+=$2} END{print s+0}' "$CACHE/records.tsv")
  files=$(awk -F'\t' -v m="$mod" '$1==m {c++} END{print c+0}' "$CACHE/records.tsv")
  classes=$(awk -F'\t' -v m="$mod" '$1==m {s+=$7} END{print s+0}' "$CACHE/records.tsv")
  ifc=$(awk -F'\t' -v m="$mod" '$1==m {s+=$8} END{print s+0}' "$CACHE/records.tsv")
  enu=$(awk -F'\t' -v m="$mod" '$1==m {s+=$9} END{print s+0}' "$CACHE/records.tsv")
  rec=$(awk -F'\t' -v m="$mod" '$1==m {s+=$10} END{print s+0}' "$CACHE/records.tsv")
  ann=$(awk -F'\t' -v m="$mod" '$1==m {s+=$11} END{print s+0}' "$CACHE/records.tsv")
  meth=$(awk -F'\t' -v m="$mod" '$1==m {s+=$12} END{print s+0}' "$CACHE/records.tsv")

  printf '  %s%s%s\n\n' "$C_BOLD" "$mod" "$C_RESET"
  printf '  %-20s %s\n' "LOC" "$(fmt_num "$total")"
  printf '  %-20s %s\n' "Files" "$(fmt_num "$files")"
  printf '  %-20s %s\n' "Classes" "$(fmt_num "$classes")"
  printf '  %-20s %s\n' "Interfaces" "$(fmt_num "$ifc")"
  printf '  %-20s %s\n' "Enums" "$(fmt_num "$enu")"
  printf '  %-20s %s\n' "Records" "$(fmt_num "$rec")"
  printf '  %-20s %s\n' "Annotation types" "$(fmt_num "$ann")"
  printf '  %-20s %s\n' "Methods (approx)" "$(fmt_num "$meth")"

  sub "Top 10 largest classes"
  awk -F'\t' -v m="$mod" '$1==m { printf "%10d  %s\n", $2, $3 }' "$CACHE/records.tsv" \
    | sort -rn | head -10 \
    | sed -E "s|$ROOT/||" | sed 's/^/  /'
}

sec_package_tree() {
  section "Package Tree"
  awk -F'\t' -v root="$ROOT" '
  $3 ~ /\/src\/main\/java\// {
    path = $3
    sub(".*/src/main/java/", "", path)
    sub(/\/[^\/]+\.java$/, "", path)
    gsub("/", ".", path)
    if (path == "") path = "(default)"
    loc[path] += $2
  }
  END { for (p in loc) printf "%s\t%d\n", p, loc[p] }
  ' "$CACHE/records.tsv" | sort > "$CACHE/pkg.txt"

  awk -F'\t' '
  {
    n = split($1, parts, ".")
    line = ""
    for (i=1; i<=n; i++) {
      line = (i==1) ? parts[i] : line "." parts[i]
      key = line
      agg[key] += $2
    }
  }
  END { for (k in agg) printf "%s\t%d\n", k, agg[k] }
  ' "$CACHE/pkg.txt" | sort | awk -F'\t' '
  {
    n = split($1, parts, ".")
    indent = ""
    for (i=1; i<n; i++) indent = indent "  "
    printf "  %s%s/  %s(%d LOC)%s\n", indent, parts[n], "\033[38;2;140;140;150m", $2, "\033[0m"
  }'
}

sec_top_modules() {
  section "Top Modules"
  awk -F'\t' '
  {
    m = $1; loc = $2
    isTest = ($3 ~ "/src/test/")
    tot[m] += loc
    if (!isTest) cl[m] += $7 + $8 + $9 + $10
    if (isTest) tloc[m] += loc
  }
  END { for (m in tot) printf "%s\t%d\t%d\t%d\n", m, tot[m], cl[m], tloc[m] }
  ' "$CACHE/records.tsv" > "$CACHE/mods.txt"

  sub "By LOC"
  sort -t$'\t' -k2,2nr "$CACHE/mods.txt" | head -10 \
    | awk -F'\t' '{ printf "  %-28s %8d  ", $1, $2; for(i=0;i<int($2/200)+1 && i<30;i++) printf "█"; print "" }'

  sub "By class count"
  sort -t$'\t' -k3,3nr "$CACHE/mods.txt" | head -10 \
    | awk -F'\t' '{ printf "  %-28s %8d  ", $1, $3; for(i=0;i<int($3/2)+1 && i<30;i++) printf "█"; print "" }'

  sub "By test LOC"
  sort -t$'\t' -k4,4nr "$CACHE/mods.txt" | head -10 \
    | awk -F'\t' '{ printf "  %-28s %8d  ", $1, $4; for(i=0;i<int($4/100)+1 && i<30;i++) printf "█"; print "" }'
}

sec_types() {
  section "Type Counters"
  awk -F'\t' '
  { c+=$7; i+=$8; e+=$9; r+=$10; a+=$11 }
  END { printf "  %-20s %s\n", "Classes", c; printf "  %-20s %s\n", "Interfaces", i;
        printf "  %-20s %s\n", "Enums", e; printf "  %-20s %s\n", "Records", r;
        printf "  %-20s %s\n", "Annotation types", a }
  ' "$CACHE/records.tsv"

  sub "Per module"
  awk -F'\t' '{ c[$1]+=$7; i[$1]+=$8; e[$1]+=$9; r[$1]+=$10; a[$1]+=$11 }
  END { for (m in c) printf "%-28s %6d %6d %6d %6d %6d\n", m, c[m], i[m], e[m], r[m], a[m] }
  ' "$CACHE/records.tsv" \
  | { printf "  %-28s %6s %6s %6s %6s %6s\n" "module" "cls" "ifc" "enum" "rec" "ann"
      printf "  %s" "$C_MUTED"; hr; cat; } \
  | sed 's/^  //;s/^/  /'
}

sec_loc_breakdown() {
  section "LOC Breakdown"
  awk -F'\t' '
  { loc+=$2; code+=$4; cmt+=$5; blank+=$6 }
  END {
    printf "  %-14s %10d\n", "Total",  loc
    printf "  %-14s %10d  (%.1f%%)\n", "Code",   code, 100*code/loc
    printf "  %-14s %10d  (%.1f%%)\n", "Comment",cmt,  100*cmt/loc
    printf "  %-14s %10d  (%.1f%%)\n", "Blank",  blank,100*blank/loc
  }' "$CACHE/records.tsv"

  sub "Per module"
  awk -F'\t' '{ loc[$1]+=$2; code[$1]+=$4; cmt[$1]+=$5 }
  END { for (m in loc) printf "%s\t%d\t%d\t%d\n", m, loc[m], code[m], cmt[m] }
  ' "$CACHE/records.tsv" | sort -t$'\t' -k2,2nr \
    | awk -F'\t' '{ printf "  %-28s %8d LOC  %5.1f%% code  %5.1f%% comment\n", $1, $2, 100*$3/$2, 100*$4/$2 }'
}

sec_largest_classes() {
  section "Top 20 Largest Classes"
  printf '  %10s  %s\n' "lines" "class"
  hr
  sort -t$'\t' -k2,2nr "$CACHE/records.tsv" | head -20 \
    | awk -F'\t' -v root="$ROOT" '{ gsub(root "/", "", $3); printf "  %10d  %s\n", $2, $3 }'
}

sec_most_methods() {
  section "Top 20 Classes by Method Count"
  printf '  %10s  %s\n' "methods" "class"
  hr
  sort -t$'\t' -k12,12nr "$CACHE/records.tsv" | head -20 \
    | awk -F'\t' -v root="$ROOT" '{ gsub(root "/", "", $3); printf "  %10d  %s\n", $12, $3 }'
}

sec_method_histogram() {
  section "Method Count Distribution"
  awk -F'\t' '
  {
    m = $12
    if (m <= 5) b["1–5"]++
    else if (m <= 10) b["6–10"]++
    else if (m <= 20) b["11–20"]++
    else b["21+"]++
    total++
  }
  END {
    order[1]="1–5"; order[2]="6–10"; order[3]="11–20"; order[4]="21+"
    for (i=1;i<=4;i++) {
      k = order[i]; v = b[k]+0
      printf "  %-8s %6d  ", k, v
      bar = int(v * 40 / (total+0.0001))
      for (j=0;j<bar;j++) printf "█"
      print ""
    }
  }' "$CACHE/records.tsv"
}

sec_annotations() {
  section "Annotation Frequency"
  head -30 "$CACHE/annotations.txt" | awk '{ printf "  %6d  %s\n", $1, $2 }'
}

sec_imports() {
  section "Import Frequency"
  head -30 "$CACHE/imports.txt" | awk '{ printf "  %6d  %s\n", $1, $2 }'
}

sec_test_ratio() {
  section "Test-to-Code Ratio"
  awk -F'\t' '
  {
    isTest = ($3 ~ "/src/test/")
    if (isTest) t[$1] += $2
    else p[$1] += $2
  }
  END { for (m in p) printf "%s\t%d\t%d\n", m, p[m], t[m]+0 }
  ' "$CACHE/records.tsv" | sort -t$'\t' -k2,2nr \
    | awk -F'\t' '{ r = ($2==0) ? 0 : $3/$2
      printf "  %-28s prod %8d  test %8d  ratio 1:%.2f\n", $1, $2, $3, r }'

  local prod test
  prod=$(awk -F'\t' '$3 !~ /\/src\/test\// {s+=$2} END{print s+0}' "$CACHE/records.tsv")
  test=$(awk -F'\t' '$3 ~ /\/src\/test\// {s+=$2} END{print s+0}' "$CACHE/records.tsv")
  hr
  printf '  %sOverall: prod %s LOC · test %s LOC · ratio 1:%.2f%s\n' \
    "$C_BOLD" "$(fmt_num "$prod")" "$(fmt_num "$test")" \
    "$(awk -v p="$prod" -v t="$test" 'BEGIN{print (p==0)?0:t/p}')" "$C_RESET"
}

sec_missing_tests() {
  section "Classes Without Test"

  awk -F'\t' '$3 ~ /\/src\/test\// {
    f = $3; sub(/.*\//, "", f); sub(/\.java$/, "", f)
    testnames[f] = 1
  }
  END { for (n in testnames) print n }
  ' "$CACHE/records.tsv" > "$CACHE/testnames.txt"

  awk -F'\t' '$3 !~ /\/src\/test\// && $7 > 0 {
    print $1 "\t" $3
  }' "$CACHE/records.tsv" > "$CACHE/prodclasses.txt"

  while IFS=$'\t' read -r mod path; do
    local base
    base=$(basename "$path" .java)
    local fq="${path#"$ROOT/"}"; fq="${fq%.java}"; fq="${fq//\//.}"
    if grep -qE "^${base}(Test|IT|Spec|Should|Tests)$" "$CACHE/testnames.txt"; then continue; fi
    printf "  %-28s %s\n" "$mod" "${path#"$ROOT/"}"
  done < "$CACHE/prodclasses.txt" | head -50

  local total
  total=$(wc -l < "$CACHE/prodclasses.txt" | tr -d ' ')
  hr
  printf '  %sClasses without test (top 50 shown, %s total prod classes).%s\n' "$C_MUTED" "$(fmt_num "$total")" "$C_RESET"
}

sec_test_annotations() {
  section "Test Annotations per Module"
  find "$ROOT" -type f -path '*/src/test/*' -name '*.java' -not -path '*/build/*' -print0 2>/dev/null \
  | xargs -0 -r grep -HohE '@(Test|ParameterizedTest|Disabled|RepeatedTest|TestFactory)' 2>/dev/null \
  | sort | uniq -c | sort -rn | head -20 \
  | awk '{ printf "  %6d  %s\n", $1, $2 }'
}

# jacoco parsing helpers
parse_jacoco_class_counters() {
  sed 's/></>\n</g' | awk '
    /<class / {
      s = $0
      if (match(s, /name="[^"]+"/)) {
        n = substr(s, RSTART+6, RLENGTH-7)
        cls = n
        inmethod = 0
      }
      next
    }
    /<method / { inmethod = 1; next }
    /<\/method>/ { inmethod = 0; next }
    /<counter type="LINE"/ && cls != "" && !inmethod {
      s = $0
      mi = ""; cv = ""
      if (match(s, /missed="[0-9]+"/))  mi = substr(s, RSTART+8, RLENGTH-9)
      if (match(s, /covered="[0-9]+"/)) cv = substr(s, RSTART+9, RLENGTH-10)
      if (mi != "" && cv != "") {
        printf "%s\t%d\t%d\n", cls, mi, cv
        cls = ""
      }
    }
  '
}

parse_jacoco_report_counters() {
  sed 's/></>\n</g' | awk '
    /<\/package>/ { next }
    /<report/ { next }
    /<sessioninfo/ { next }
    # Report-level counters are at the end (outside of packages).
    # Easiest: capture counters that appear after last </package>
    { lines[NR] = $0 }
    END {
      # find last </package>
      last = 0
      for (i=1;i<=NR;i++) if (lines[i] ~ /<\/package>/) last = i
      for (i=last+1;i<=NR;i++) {
        s = lines[i]
        if (s ~ /<counter type="LINE"/) {
          match(s, /missed="[0-9]+"/); ml = substr(s, RSTART+8, RLENGTH-9)
          match(s, /covered="[0-9]+"/); cl = substr(s, RSTART+9, RLENGTH-10)
        }
        if (s ~ /<counter type="BRANCH"/) {
          match(s, /missed="[0-9]+"/); mb = substr(s, RSTART+8, RLENGTH-9)
          match(s, /covered="[0-9]+"/); cb = substr(s, RSTART+9, RLENGTH-10)
        }
        if (s ~ /<counter type="METHOD"/) {
          match(s, /missed="[0-9]+"/); mm = substr(s, RSTART+8, RLENGTH-9)
          match(s, /covered="[0-9]+"/); cm = substr(s, RSTART+9, RLENGTH-10)
        }
      }
      printf "%d\t%d\t%d\t%d\t%d\t%d\n", ml+0, cl+0, mb+0, cb+0, mm+0, cm+0
    }
  '
}

sec_jacoco_overall() {
  section "JaCoCo Overall"
  if [[ ! -s "$CACHE/jacoco.txt" ]]; then
    printf '  %sNo JaCoCo XMLs found under **/build/reports/jacoco/**.%s\n' "$C_WARN" "$C_RESET"
    printf '  %sRun: ./gradlew test jacocoTestReport%s\n' "$C_MUTED" "$C_RESET"
    return
  fi
  local ml=0 cl=0 mb=0 cb=0 mm=0 cm=0
  while IFS= read -r xml; do
    read -r a b c d e f < <(parse_jacoco_report_counters < "$xml")
    ml=$((ml+a)); cl=$((cl+b)); mb=$((mb+c)); cb=$((cb+d)); mm=$((mm+e)); cm=$((cm+f))
  done < "$CACHE/jacoco.txt"

  local line_p branch_p method_p
  line_p=$(pct "$cl" "$((cl+ml))")
  branch_p=$(pct "$cb" "$((cb+mb))")
  method_p=$(pct "$cm" "$((cm+mm))")

  printf '  %-12s %6s%%  ' "Line" "$line_p"
  printf '%s' "$(case "$(color_for_pct "$line_p")" in ok) echo "$C_OK";; warn) echo "$C_WARN";; *) echo "$C_BAD";; esac)"
  bar "$cl" "$((cl+ml))" 40
  printf '%s\n' "$C_RESET"

  printf '  %-12s %6s%%  ' "Branch" "$branch_p"
  printf '%s' "$(case "$(color_for_pct "$branch_p")" in ok) echo "$C_OK";; warn) echo "$C_WARN";; *) echo "$C_BAD";; esac)"
  bar "$cb" "$((cb+mb))" 40
  printf '%s\n' "$C_RESET"

  printf '  %-12s %6s%%  ' "Method" "$method_p"
  printf '%s' "$(case "$(color_for_pct "$method_p")" in ok) echo "$C_OK";; warn) echo "$C_WARN";; *) echo "$C_BAD";; esac)"
  bar "$cm" "$((cm+mm))" 40
  printf '%s\n' "$C_RESET"
}

sec_jacoco_per_module() {
  section "JaCoCo per Module"
  if [[ ! -s "$CACHE/jacoco.txt" ]]; then
    printf '  %sNo JaCoCo XMLs found.%s\n' "$C_WARN" "$C_RESET"; return
  fi
  printf '  %-28s %8s %8s %8s\n' "module" "line%" "branch%" "method%"
  hr
  while IFS= read -r xml; do
    local mod
    mod=$(module_of_path "$xml")
    read -r a b c d e f < <(parse_jacoco_report_counters < "$xml")
    local lp bp mp
    lp=$(pct "$b" "$((b+a))")
    bp=$(pct "$d" "$((d+c))")
    mp=$(pct "$f" "$((f+e))")
    printf '  %-28s %7s%% %7s%% %7s%%\n' "$mod" "$lp" "$bp" "$mp"
  done < "$CACHE/jacoco.txt"
}

collect_jacoco_classes() {
  : > "$CACHE/jacoco_classes.tsv"
  while IFS= read -r xml; do
    local mod
    mod=$(module_of_path "$xml")
    parse_jacoco_class_counters < "$xml" \
      | awk -F'\t' -v m="$mod" '{ print m "\t" $0 }'
  done < "$CACHE/jacoco.txt" >> "$CACHE/jacoco_classes.tsv"
}

sec_jacoco_worst() {
  section "Worst 10 Classes by Line Coverage"
  [[ -s "$CACHE/jacoco.txt" ]] || { printf '  %sNo JaCoCo data.%s\n' "$C_WARN" "$C_RESET"; return; }
  collect_jacoco_classes
  awk -F'\t' '{ p = ($3+$4==0)?100:100*$4/($3+$4); printf "%s\t%s\t%d\t%d\t%.1f\n", $1, $2, $3, $4, p }' \
    "$CACHE/jacoco_classes.tsv" \
  | sort -t$'\t' -k5,5n | head -10 \
  | awk -F'\t' '{ printf "  %5.1f%%  %-28s  %s\n", $5, $1, $2 }'
}

sec_jacoco_best() {
  section "Best 10 Classes by Line Coverage"
  [[ -s "$CACHE/jacoco.txt" ]] || { printf '  %sNo JaCoCo data.%s\n' "$C_WARN" "$C_RESET"; return; }
  collect_jacoco_classes
  awk -F'\t' '{ if ($3+$4<3) next; p = 100*$4/($3+$4); printf "%s\t%s\t%d\t%d\t%.1f\n", $1, $2, $3, $4, p }' \
    "$CACHE/jacoco_classes.tsv" \
  | sort -t$'\t' -k5,5nr | head -10 \
  | awk -F'\t' '{ printf "  %5.1f%%  %-28s  %s\n", $5, $1, $2 }'
}

# Gradle dependency parsing
extract_gradle_deps() {
  local file="$1"
  [[ -f "$file" ]] || return
  grep -oE "(implementation|api|compileOnly|runtimeOnly|testImplementation|testCompileOnly|testRuntimeOnly|annotationProcessor|kapt|classpath)[[:space:]]*[([[:space:]]+['\"][^'\"]+['\"]" "$file" 2>/dev/null \
    | sed -E "s/[[:space:]]*[([[:space:]]+['\"]/ /; s/['\"]$//" \
    | awk '{ scope=$1; coord=$2; print scope "\t" coord }'
}

sec_deps_per_module() {
  section "Dependencies per Module"
  local mod
  for mod in "${MODULES[@]}"; do
    local f=""
    [[ -f "$ROOT/$mod/build.gradle.kts" ]] && f="$ROOT/$mod/build.gradle.kts"
    [[ -z "$f" && -f "$ROOT/$mod/build.gradle" ]] && f="$ROOT/$mod/build.gradle"
    [[ -z "$f" ]] && continue
    local n
    n=$(extract_gradle_deps "$f" | wc -l | tr -d ' ')
    printf '  %-28s %4d deps\n' "$mod" "$n"
  done

  sub "Duplicate coordinates across modules"
  for mod in "${MODULES[@]}"; do
    local f=""
    [[ -f "$ROOT/$mod/build.gradle.kts" ]] && f="$ROOT/$mod/build.gradle.kts"
    [[ -z "$f" && -f "$ROOT/$mod/build.gradle" ]] && f="$ROOT/$mod/build.gradle"
    [[ -z "$f" ]] && continue
    extract_gradle_deps "$f" | awk -F'\t' -v m="$mod" '{ print $2 "\t" m }'
  done | sort | awk -F'\t' '{ c[$1] = c[$1] " " $2 } END { for (k in c) { n = split(c[k], arr, " "); if (n > 1) printf "  %-50s %s\n", k, c[k] } }' | sort | head -20
}

sec_version_conflicts() {
  section "Version Conflicts"
  local mod
  for mod in "${MODULES[@]}"; do
    local f=""
    [[ -f "$ROOT/$mod/build.gradle.kts" ]] && f="$ROOT/$mod/build.gradle.kts"
    [[ -z "$f" && -f "$ROOT/$mod/build.gradle" ]] && f="$ROOT/$mod/build.gradle"
    [[ -z "$f" ]] && continue
    extract_gradle_deps "$f" | awk -F'\t' '{ print $2 }'
  done | grep -E '^[^:]+:[^:]+:[^:]+$' \
    | awk -F: '{ print $1":"$2"\t"$3 }' \
    | sort -u \
    | awk -F'\t' '{ v[$1] = v[$1] " " $2 } END {
        for (k in v) {
          n = split(v[k], arr, " ")
          if (n > 1) {
            # unique check
            delete u; cnt = 0
            for (i=1;i<=n;i++) if (!(arr[i] in u)) { u[arr[i]]=1; cnt++ }
            if (cnt > 1) printf "  %-50s %s\n", k, v[k]
          }
        }
      }' | sort
}

sec_dep_outliers() {
  section "Dependency Outliers"
  local mod
  for mod in "${MODULES[@]}"; do
    local f=""
    [[ -f "$ROOT/$mod/build.gradle.kts" ]] && f="$ROOT/$mod/build.gradle.kts"
    [[ -z "$f" && -f "$ROOT/$mod/build.gradle" ]] && f="$ROOT/$mod/build.gradle"
    [[ -z "$f" ]] && continue
    local n
    n=$(extract_gradle_deps "$f" | wc -l | tr -d ' ')
    printf '%s\t%d\n' "$mod" "$n"
  done | sort -t$'\t' -k2,2nr \
    | awk -F'\t' 'NR==1 { max=$2 } { printf "  %-28s %4d  ", $1, $2; bar=int($2*30/(max+0.0001)); for(i=0;i<bar;i++)printf "█"; print "" }'
}

sec_hotspots() {
  section "Hotspots (last 6 months)"
  if [[ ! -s "$CACHE/hotspots.txt" ]]; then
    printf '  %sNo git history available.%s\n' "$C_WARN" "$C_RESET"; return
  fi
  printf '  %6s  %s\n' "commits" "file"
  hr
  head -20 "$CACHE/hotspots.txt" | awk '{ printf "  %6d  %s\n", $1, $2 }'
}

sec_todos() {
  section "TODOs by Module"
  if [[ ! -s "$CACHE/todos.txt" ]]; then
    printf '  %sNo TODOs found.%s\n' "$C_OK" "$C_RESET"; return
  fi
  # Count per module
  while IFS=: read -r path _ _; do
    local mod
    mod=$(module_of_path "$path")
    echo "$mod"
  done < "$CACHE/todos.txt" | sort | uniq -c | sort -rn \
    | awk '{ printf "  %5d  %s\n", $1, $2 }'

  sub "Oldest TODOs (git blame)"
  if git -C "$ROOT" rev-parse --git-dir >/dev/null 2>&1; then
    head -20 "$CACHE/todos.txt" | while IFS=: read -r path line rest; do
      local ts
      ts=$(git -C "$ROOT" blame -L "$line,$line" --porcelain -- "$path" 2>/dev/null | head -1 | awk '{print $1}')
      [[ -z "$ts" ]] && continue
      local date
      date=$(git -C "$ROOT" show -s --format=%cs "$ts" 2>/dev/null)
      printf '  %s  %s:%s\n' "$date" "${path#"$ROOT/"}" "$line"
    done | sort | head -10
  else
    printf '  %s(no git)%s\n' "$C_MUTED" "$C_RESET"
  fi
}

sec_smells() {
  section "Code Smells"
  if [[ ! -s "$CACHE/smells.txt" ]]; then
    printf '  %sClean. No classic smells detected.%s\n' "$C_OK" "$C_RESET"; return
  fi
  awk -F: '{ print $3 }' "$CACHE/smells.txt" 2>/dev/null | sort | uniq -c | sort -rn | head -10 \
    | awk '{ printf "  %5d  %s\n", $1, $2 }'
  sub "Details (top 20)"
  head -20 "$CACHE/smells.txt" | while IFS=: read -r path line rest; do
    printf '  %s:%s  %s\n' "${path#"$ROOT/"}" "$line" "$(echo "$rest" | sed 's/^[ \t]*//' | cut -c1-70)"
  done
}

sec_file_hygiene() {
  section "File Hygiene"
  sub "CRLF line endings"
  if [[ -s "$CACHE/crlf.txt" ]]; then
    wc -l < "$CACHE/crlf.txt" | tr -d ' ' | xargs -I{} printf '  {} files with CRLF\n'
    head -5 "$CACHE/crlf.txt" | sed "s|$ROOT/|  |"
  else
    printf '  %sNone.%s\n' "$C_OK" "$C_RESET"
  fi

  sub "Classes > 500 lines"
  awk -F'\t' '$2 > 500 { printf "  %5d  %s\n", $2, $3 }' "$CACHE/records.tsv" \
    | sort -rn | head -10 | sed "s|$ROOT/||g"

  sub "Duplicate simple class names across packages"
  awk -F'\t' '{ n=$3; sub(/.*\//,"",n); sub(/\.java$/,"",n); if (n == "package-info") next; print n "\t" $3 }' "$CACHE/records.tsv" \
    | sort | awk -F'\t' '{ c[$1]++; if (c[$1]==1) first[$1]=$2; else if (c[$1]==2) { print "  " $1; print "    " first[$1]; print "    " $2 } }' \
    | head -20 | sed "s|$ROOT/||g"
}

# Dispatch
declare -a SECTIONS=(
  "1  Module Overview"
  "2  Module Detail"
  "3  Package Tree"
  "4  Top Modules"
  "5  Type Counters"
  "6  LOC Breakdown"
  "7  Largest Classes"
  "8  Most Methods"
  "9  Method Distribution"
  "10 Annotation Frequency"
  "11 Import Frequency"
  "12 Test-to-Code Ratio"
  "13 Classes Without Test"
  "14 Test Annotations"
  "15 JaCoCo Overall"
  "16 JaCoCo per Module"
  "17 JaCoCo Worst Classes"
  "18 JaCoCo Best Classes"
  "19 Dependencies per Module"
  "20 Version Conflicts"
  "21 Dependency Outliers"
  "22 Hotspots"
  "23 TODOs"
  "24 Code Smells"
  "25 File Hygiene"
)

dispatch() {
  local key="$1"
  case "$key" in
    1|modules)               sec_modules ;;
    2|module)                sec_module_detail "${2:-}" ;;
    3|packages)              sec_package_tree ;;
    4|top)                   sec_top_modules ;;
    5|types)                 sec_types ;;
    6|loc)                   sec_loc_breakdown ;;
    7|largest)               sec_largest_classes ;;
    8|methods)               sec_most_methods ;;
    9|histogram)             sec_method_histogram ;;
    10|annotations)          sec_annotations ;;
    11|imports)              sec_imports ;;
    12|test-ratio)           sec_test_ratio ;;
    13|missing-tests)        sec_missing_tests ;;
    14|test-annotations)     sec_test_annotations ;;
    15|jacoco)               sec_jacoco_overall ;;
    16|jacoco-modules)       sec_jacoco_per_module ;;
    17|jacoco-worst)         sec_jacoco_worst ;;
    18|jacoco-best)          sec_jacoco_best ;;
    19|deps)                 sec_deps_per_module ;;
    20|conflicts)            sec_version_conflicts ;;
    21|dep-outliers)         sec_dep_outliers ;;
    22|hotspots)             sec_hotspots ;;
    23|todos)                sec_todos ;;
    24|smells)               sec_smells ;;
    25|hygiene)              sec_file_hygiene ;;
    *) echo "Unknown section: $key" >&2; return 1 ;;
  esac
}

run_all() {
  for s in "${SECTIONS[@]}"; do
    local key="${s%% *}"
    dispatch "$key"
    echo
  done
}

menu() {
  if ! command -v fzf >/dev/null 2>&1; then
    printf '%s\n' "${C_BAD}fzf not found.${C_RESET}"
    printf '%s\n' "${C_MUTED}Install fzf, or run with an argument, e.g.:${C_RESET}"
    printf '  %sstats.sh all%s\n' "$C_ACCENT" "$C_RESET"
    printf '  %sstats.sh modules%s\n' "$C_ACCENT" "$C_RESET"
    printf '  %sstats.sh jacoco%s%s\n' "$C_ACCENT" "${C_MUTED} (and: " "$C_RESET"
    printf '  %s   module  packages  top  types  loc  largest  methods  histogram%s\n' "$C_MUTED" "$C_RESET"
    printf '  %s   annotations  imports  test-ratio  missing-tests  test-annotations%s\n' "$C_MUTED" "$C_RESET"
    printf '  %s   jacoco-modules  jacoco-worst  jacoco-best  deps  conflicts%s\n' "$C_MUTED" "$C_RESET"
    printf '  %s   dep-outliers  hotspots  todos  smells  hygiene%s\n' "$C_MUTED" "$C_RESET"
    exit 1
  fi
  local last_pos=1
  while :; do
    local pick
    pick=$(printf '%s\n' "${SECTIONS[@]}" | fzf \
      --prompt='> ' \
      --height=~60% \
      --reverse \
      --border=rounded \
      --header="esc: quit" \
      --bind "start:pos($last_pos)" \
      --color='fg:250,fg+:#75b4ff,hl:#75b4ff,hl+:#b48cff,prompt:#b48cff,pointer:#e6b450') || break
    [[ -z "$pick" ]] && break
    local key="${pick%% *}"
    last_pos="$key"
    clear
    dispatch "$key"
    printf '\n%senter to continue%s' "$C_MUTED" "$C_RESET"
    read -r _ || break
    clear
  done
  clear
}

usage() {
  cat <<EOF
Usage: stats.sh [command] [args]

Commands:
  (none)            fzf menu
  all               print every section
  modules           §1   Module overview
  module <name>     §2   Single module detail
  packages          §3   Package tree with LOC
  top               §4   Top modules by LOC / classes / test-LOC
  types             §5   Class/interface/enum/record counters
  loc               §6   Code/comment/blank breakdown
  largest           §7   Top 20 largest classes
  methods           §8   Top 20 classes by method count
  histogram         §9   Method count distribution
  annotations       §10  Annotation frequency
  imports           §11  Import frequency
  test-ratio        §12  Test-to-code ratio per module
  missing-tests     §13  Classes without test
  test-annotations  §14  @Test/@ParameterizedTest/@Disabled
  jacoco            §15  JaCoCo overall (line/branch/method)
  jacoco-modules    §16  JaCoCo per module
  jacoco-worst      §17  Worst classes by line coverage
  jacoco-best       §18  Best classes by line coverage
  deps              §19  Dependencies per module + duplicates
  conflicts         §20  Version conflicts
  dep-outliers      §21  Modules with unusually many deps
  hotspots          §22  Most changed files (git, 6 months)
  todos             §23  TODO/FIXME/XXX/HACK counts + oldest
  smells            §24  System.out, printStackTrace, empty catches
  hygiene           §25  CRLF, huge classes, duplicate simple names

Env:
  NO_COLOR=1        disable colors
EOF
}

# Main
detect_modules
build_cache

cmd="${1:-}"
case "$cmd" in
  ""|menu)              menu ;;
  all)                  run_all ;;
  module)               dispatch 2 "${2:-}" ;;
  -h|--help|help)       usage ;;
  *)                    dispatch "$cmd" "${2:-}" ;;
esac