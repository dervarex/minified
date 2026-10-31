#!/usr/bin/env python3
"""
matrix.py jobs     [--loaders a,b] [--versions x,y] [--pairs FILE]   print the GitHub Actions job matrix as JSON
matrix.py discover [--dry-run] [--pairs-out FILE]               add new Minecraft releases and new loader builds to versions.toml
matrix.py merge    [DIR] [--jobs FILE] [--run-url URL]            merge test result files into results.json
matrix.py render   [-o FILE]                                      draw results.json as SVG
"""
import argparse
import json
import re
import sys
import time
from datetime import datetime, timezone
import tomllib
import urllib.error
import urllib.request
from dataclasses import dataclass
from html import escape
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent.parent
CONFIG = HERE / "versions.toml"
RESULTS = HERE / "results.json"


# Look

# status -> legend text
STATUSES = {
    "pass": "passed",
    "fail": "failed",
    "skipped": "skipped",
}

# light and dark theme for github
THEMES = {
    "light": {
        "bg": "#ffffff", "border": "#d1d9e0", "fg": "#1f2328", "muted": "#59636e", "faint": "#818b98", "rule": "#d1d9e0",
        "missing": "#eff2f5", "pass": "#2da44e", "fail": "#cf222e", "skipped": "#bf8700",
    },
    "dark": {
        "bg": "#0d1117", "border": "#3d444d", "fg": "#f0f6fc", "muted": "#9198a1", "faint": "#656c76", "rule": "#262c36",
        "missing": "#21262d", "pass": "#238636", "fail": "#da3633", "skipped": "#9e6a03",
    },
}

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans', Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace"

PAD = 32              # around everything
LABEL_W = 100         # loader name column, also holds the group name
CELL_W, CELL_H = 52, 22
GAP_X, GAP_Y = 6, 6
MAX_COLS = 14         # groups with more versions wrap into another block
LABEL_CHARS = 7       # longer version names get cut at the first - or _ (rd-132211 -> rd), the full name is in the tooltip


# Config

@dataclass
class Group:
    name: str
    versions: list[str]


@dataclass
class Loader:
    id: str
    label: str
    versions: list[str]
    # versions the loader has no build for yet, discover adds them to versions once it has
    pending: list[str]


@dataclass
class Config:
    groups: list[Group]
    loaders: list[Loader]

    @property
    def versions(self):
        return [v for g in self.groups for v in g.versions]

    def pairs(self):
        return [(l.id, v) for l in self.loaders for v in l.versions]


def load_config(path=CONFIG):
    raw = tomllib.loads(path.read_text())
    groups = [Group(g["name"], g["versions"]) for g in raw["group"]]
    order = [v for g in groups for v in g.versions]
    if dupes := sorted({v for v in order if order.count(v) > 1}):
        sys.exit(f"{path.name}: in more than one group: {', '.join(dupes)}")

    def expand(loader, entries):
        picked = set()
        for entry in entries:
            first, _, last = entry.partition("..")
            last = last or first
            for v in (first, last):
                if v not in order:
                    sys.exit(f"{path.name}: loader {loader}: {v} isn't in any group")
            a, b = order.index(first), order.index(last)
            if a > b:
                sys.exit(f"{path.name}: loader {loader}: {entry} is backwards")
            picked.update(order[a:b + 1])
        return [v for v in order if v in picked]

    loaders = []
    for id, spec in raw["loader"].items():
        versions = expand(id, spec["versions"])
        pending = [v for v in order if v in spec.get("pending", [])]
        if unknown := [v for v in spec.get("pending", []) if v not in order]:
            sys.exit(f"{path.name}: loader {id}: pending {', '.join(unknown)} isn't in any group")
        if both := [v for v in pending if v in versions]:
            sys.exit(f"{path.name}: loader {id}: {', '.join(both)} is in versions and pending")
        loaders.append(Loader(id, spec.get("label", id), versions, pending))
    return Config(groups, loaders)


# Results

def load_results(path=RESULTS):
    if not path.exists():
        return {}
    return {(r["loader"], r["mcVersion"]): r for r in json.loads(path.read_text())}


def save_results(results, config, path=RESULTS):
    order = {pair: i for i, pair in enumerate(config.pairs())}
    rows = sorted(results.values(), key=lambda r: order[r["loader"], r["mcVersion"]])
    # one result per line keeps the diffs of the bot's commits readable
    path.write_text("[\n" + ",\n".join("  " + json.dumps(r, ensure_ascii=False) for r in rows) + "\n]\n")


# Commands

NO_RESULT_MESSAGE = "The job ended before the test wrote a result (failed setup step or timeout)"

def split(arg):
    return [s.strip() for s in (arg or "").split(",") if s.strip()]


def cmd_jobs(args):
    config = load_config()
    loaders, versions = split(args.loaders), split(args.versions)
    for name, wanted, known in (("loader", loaders, [l.id for l in config.loaders]), ("version", versions, config.versions)):
        if unknown := [w for w in wanted if w not in known]:
            sys.exit(f"unknown {name}: {', '.join(unknown)} (see {CONFIG.name})")

    only = None
    if args.pairs:
        only = {tuple(p) for p in json.loads(Path(args.pairs).read_text())}

    jobs = [{"loader": l, "mc_version": v} for l, v in config.pairs()
            if (not loaders or l in loaders) and (not versions or v in versions) and (only is None or (l, v) in only)]
    if not jobs and not args.allow_empty:
        sys.exit("no loader supports any of these versions")
    print(json.dumps({"include": jobs}, separators=(",", ":")))


def cmd_merge(args):
    config = load_config()
    results = load_results()
    expected = set(config.pairs())
    files = sorted(Path(args.dir).rglob("*.json")) if Path(args.dir).is_dir() else []
    if not files:
        print(f"no results in {args.dir}, nothing to merge")

    # jobs of this run that ended before the test wrote its result (setup step failed, timeout) count as failed
    reported = set()
    for file in files:
        try:
            r = json.loads(file.read_text())
            reported.add((r["loader"], r["mcVersion"]))
        except (json.JSONDecodeError, KeyError, TypeError):
            pass
    no_result = []
    if args.jobs:
        for job in json.loads(Path(args.jobs).read_text())["include"]:
            key = (job["loader"], job["mc_version"])
            if key in expected and key not in reported:
                no_result.append({"loader": key[0], "mcVersion": key[1], "status": "fail",
                                  "message": NO_RESULT_MESSAGE, "timestamp": datetime.now(timezone.utc).isoformat()})

    changed = 0
    for file_name, r in [*((f.name, f) for f in files), *((f"{j['loader']} {j['mcVersion']}", j) for j in no_result)]:
        try:
            if isinstance(r, Path):
                r = json.loads(r.read_text())
            key = (r["loader"], r["mcVersion"])
            status = r["status"]
        except (json.JSONDecodeError, KeyError, TypeError) as e:
            print(f"skipping {file_name}: {e!r}")
            continue
        if key not in expected:
            print(f"skipping {file_name}: {key[0]} {key[1]} isn't in {CONFIG.name}")
            continue
        if status not in STATUSES:
            print(f"skipping {file_name}: unknown status {status!r}")
            continue
        old = results.get(key)
        if old and old["status"] == status and old.get("message") == r.get("message"):
            continue
        results[key] = {k: r.get(k, "") for k in ("loader", "mcVersion", "status", "message", "timestamp")}
        if args.run_url:
            # the run the current status started in, the issue report links its logs
            results[key]["run"] = args.run_url
        changed += 1

    for key in [k for k in results if k not in expected]:
        print(f"dropping {key[0]} {key[1]}, it isn't in {CONFIG.name} anymore")
        del results[key]
        changed += 1

    save_results(results, config)
    print(f"{changed} change(s), {len(results)}/{len(expected)} pairs have a result")


def cmd_render(args):
    config = load_config()
    out = Path(args.output)
    out.write_text(render(config, load_results()))
    print(f"wrote {out}")


# Discover

MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"


def fetch(url, tries=3):
    """None if there is nothing (Fabric answers unknown versions with 400, the others with 404), the body otherwise"""
    for attempt in range(tries):
        try:
            request = urllib.request.Request(url, headers={"User-Agent": "minified-version-matrix"})
            with urllib.request.urlopen(request, timeout=30) as response:
                return response.read().decode()
        except urllib.error.HTTPError as e:
            if e.code in (400, 404):
                return None
            if attempt == tries - 1:
                raise
        except (urllib.error.URLError, TimeoutError):
            if attempt == tries - 1:
                raise
        time.sleep(5 * (attempt + 1))


def fetch_json(url):
    body = fetch(url)
    return None if body is None else json.loads(body)


def neoforge_prefix(mc):
    # same as NeoVersionFetcher.toNeoForgeVersionPrefix: 1.21.1 -> 21.1. and 26.1 -> 26.1.0.
    parts = mc.split(".")
    length = 3
    if parts[0] == "1":
        parts, length = parts[1:], 2
    parts += ["0"] * (length - len(parts))
    return ".".join(parts) + "."


class Builds:
    """Asks each loader's own meta whether it has a build for a Minecraft version, the same sources minified-launch uses"""

    def __init__(self):
        self._forge = None
        self._neoforge = None

    def has(self, loader, mc):
        match loader:
            case "vanilla":
                return True
            case "fabric":
                return bool(fetch_json(f"https://meta.fabricmc.net/v2/versions/loader/{mc}"))
            case "quilt":
                return bool(fetch_json(f"https://meta.quiltmc.org/v3/versions/loader/{mc}"))
            case "forge":
                if self._forge is None:
                    self._forge = fetch_json("https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json")["promos"]
                return f"{mc}-latest" in self._forge or f"{mc}-recommended" in self._forge
            case "neoforge":
                if self._neoforge is None:
                    self._neoforge = fetch_json("https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge")["versions"]
                return any(v.startswith(neoforge_prefix(mc)) for v in self._neoforge)
        sys.exit(f"don't know how to look up {loader} builds, add it to Builds.has in {Path(__file__).name}")


def new_releases(config):
    """Releases newer than the newest one in versions.toml, oldest first"""
    releases = [v["id"] for v in fetch_json(MANIFEST_URL)["versions"] if v["type"] == "release"]  # newest first
    known = set(config.versions)
    newest_known = next((i for i, v in enumerate(releases) if v in known), None)
    if newest_known is None:
        sys.exit(f"none of the releases in {CONFIG.name} are in the version manifest")
    return list(reversed(releases[:newest_known]))


def group_for(version, groups):
    """26.4 goes into "26.x", 27.1 gets a new "27.x" group"""
    name = version.split(".")[0] + ".x"
    for group in groups:
        if group.name == name:
            return group
    return None


def compact(versions, order):
    """["a", "b", "c", "e"] -> ["a..c", "e"], runs follow the group order"""
    picked = set(versions)
    runs, run = [], []
    for v in order:
        if v in picked:
            run.append(v)
        elif run:
            runs.append(run)
            run = []
    if run:
        runs.append(run)
    return [r[0] if len(r) == 1 else f"{r[0]}..{r[-1]}" for r in runs]


def toml_list(items):
    return "[" + ", ".join(json.dumps(i) for i in items) + "]"


def write_config(config, added_groups, path=CONFIG):
    """Rewrites only the lines that changed, so comments and layout stay"""
    order = config.versions
    groups = {g.name: g for g in config.groups}
    loaders = {l.id: l for l in config.loaders}
    out = []
    table = None             # ("group", name) or ("loader", id)
    last_group_line = None   # new groups go after the versions line of the last group
    pending_line = {}        # loader id -> index of its pending line in out
    loader_end = {}          # loader id -> index after its versions line in out

    for line in path.read_text().splitlines():
        stripped = line.strip()
        key = stripped.split("=", 1)[0].strip() if "=" in stripped and not stripped.startswith("#") else None
        if stripped == "[[group]]":
            table = ("group", None)
        elif m := re.fullmatch(r"\[loader\.([\w-]+)\]", stripped):
            table = ("loader", m.group(1))
        elif table and table[0] == "group" and key == "name":
            table = ("group", tomllib.loads(stripped)["name"])
        elif table and key == "versions":
            if table[0] == "group":
                line = f"versions = {toml_list(groups[table[1]].versions)}"
                last_group_line = len(out)
            else:
                line = f"versions = {toml_list(compact(loaders[table[1]].versions, order))}"
                loader_end[table[1]] = len(out) + 1
        elif table and table[0] == "loader" and key == "pending":
            pending_line[table[1]] = len(out)
        out.append(line)

    # loaders: update, add or drop the pending line, back to front so the indices stay right
    edits = []
    for id, loader in loaders.items():
        text = f"pending = {toml_list(loader.pending)}" if loader.pending else None
        if id in pending_line:
            edits.append((pending_line[id], 1, [text] if text else []))
        elif text:
            edits.append((loader_end[id], 0, [text]))
    if added_groups:
        block = []
        for g in added_groups:
            block += ["", "[[group]]", f"name = {json.dumps(g.name)}", f"versions = {toml_list(g.versions)}"]
        edits.append((last_group_line + 1, 0, block))
    for index, remove, lines in sorted(edits, key=lambda e: e[0], reverse=True):
        out[index:index + remove] = lines

    path.write_text("\n".join(out) + "\n")


def cmd_discover(args):
    config = load_config()
    builds = Builds()

    # 1. new Minecraft releases become columns, every loader either supports them or waits for a build
    added_groups = []
    releases = new_releases(config)
    for version in releases:
        group = group_for(version, config.groups)
        if group is None:
            group = Group(version.split(".")[0] + ".x", [])
            config.groups.append(group)
            added_groups.append(group)
        group.versions.append(version)
        for loader in config.loaders:
            loader.pending.append(version)

    # 2. pending versions move to versions as soon as the loader has a build
    new_pairs = []
    for loader in config.loaders:
        still = []
        for version in loader.pending:
            if builds.has(loader.id, version):
                loader.versions.append(version)
                new_pairs.append((loader.id, version))
            else:
                still.append(version)
        loader.pending = still

    order = config.versions
    for loader in config.loaders:
        loader.versions = [v for v in order if v in loader.versions]
        loader.pending = [v for v in order if v in loader.pending]

    for version in releases:
        print(f"new Minecraft release {version}")
    for loader_id, version in new_pairs:
        print(f"new pair {loader_id} {version}")
    for loader in config.loaders:
        if loader.pending:
            print(f"{loader.id} has no build yet for {', '.join(loader.pending)}")
    if not releases and not new_pairs:
        print("nothing new")

    if (releases or new_pairs) and not args.dry_run:
        write_config(config, added_groups)
        # parse what was written, a broken versions.toml must not get committed
        written = load_config()
        assert set(written.pairs()) == set(config.pairs()), "versions.toml was written wrong"
        assert written.versions == order, "versions.toml was written wrong"
        print(f"updated {CONFIG.name}")

    if args.pairs_out:
        Path(args.pairs_out).write_text(json.dumps([list(p) for p in new_pairs]))


# SVG

def short(version):
    if len(version) <= LABEL_CHARS:
        return version
    return version.replace("_", "-").split("-")[0]


def chunks(items, size):
    return [items[i:i + size] for i in range(0, len(items), size)]


def style():
    def colors(t):
        rules = [
            f".card{{fill:{t['bg']};stroke:{t['border']}}}",
            f".fg{{fill:{t['fg']}}}.muted{{fill:{t['muted']}}}.faint{{fill:{t['faint']}}}",
            f".rule{{stroke:{t['rule']}}}",
        ]
        rules += [f".{s}{{fill:{t[s]}}}" for s in [*STATUSES, "missing"]]
        rules.append(f".pending{{fill:none;stroke:{t['faint']};stroke-dasharray:3 3}}")
        return "".join(rules)

    return (f"text{{font-family:{FONT}}}.mono{{font-family:{MONO}}}"
            + colors(THEMES["light"])
            + f"@media (prefers-color-scheme:dark){{{colors(THEMES['dark'])}}}")


def render(config, results):
    labels = {l.id: l.label for l in config.loaders}
    supported = set(config.pairs())
    pending = {(l.id, v) for l in config.loaders for v in l.pending}
    status_of = lambda l, v: results[l, v]["status"] if (l, v) in results else "missing"

    cols = min(MAX_COLS, max(len(g.versions) for g in config.groups))
    width = PAD * 2 + LABEL_W + cols * (CELL_W + GAP_X) - GAP_X
    right = width - PAD
    grid_x = PAD + LABEL_W
    body = []
    y = PAD

    # header: title, summary, legend
    counts = {s: 0 for s in [*STATUSES, "missing"]}
    for pair in config.pairs():
        counts[status_of(*pair)] += 1

    body.append(f'<text x="{PAD}" y="{y + 22}" font-size="24" font-weight="600" class="fg">Supported Versions</text>')

    legend = [(s, counts[s], text) for s, text in STATUSES.items() if counts[s] or s != "skipped"]
    if counts["missing"]:
        legend.append(("faint", counts["missing"], "not tested"))
    if pending:
        legend.append(("faint", len(pending), "no loader build yet"))
    spans = "".join(f'<tspan dx="{0 if i == 0 else 16}" class="{cls}" font-weight="600">{count}</tspan>'
                    f'<tspan dx="4" class="muted">{escape(text)}</tspan>' for i, (cls, count, text) in enumerate(legend))
    body.append(f'<text x="{right}" y="{y + 22}" font-size="13" text-anchor="end">{spans}</text>')
    y += 48

    # one block per group
    for group in config.groups:
        for block_no, block in enumerate(chunks(group.versions, cols)):
            loaders = [l for l in config.loaders if any((l.id, v) in supported | pending for v in block)]
            if not loaders:
                continue
            y += 20
            if block_no == 0:
                body.append(f'<text x="{PAD}" y="{y}" font-size="13" font-weight="600" class="fg">{escape(group.name)}</text>')
            heads = "".join(f'<text x="{grid_x + i * (CELL_W + GAP_X) + CELL_W / 2:g}" y="{y}">{escape(short(v))}'
                            f'<title>{escape(v)}</title></text>' for i, v in enumerate(block))
            body.append(f'<g font-size="11" text-anchor="middle" class="mono muted">{heads}</g>')
            y += 10
            body.append(f'<line x1="{PAD}" y1="{y + .5}" x2="{right}" y2="{y + .5}" class="rule"/>')
            y += 8

            for loader in loaders:
                mid = y + CELL_H / 2
                body.append(f'<text x="{PAD}" y="{mid + 4.5:g}" font-size="13" class="fg">{escape(loader.label)}</text>')
                for i, v in enumerate(block):
                    x = grid_x + i * (CELL_W + GAP_X)
                    body.append(cell(loader, v, x, y, status_of(loader.id, v), results.get((loader.id, v)), supported, pending))
                y += CELL_H + GAP_Y
            y += 8

    # failures, since a README can't show tooltips
    failed = [results[p] for p in config.pairs() if status_of(*p) == "fail"]
    if failed:
        y += 24
        body.append(f'<text x="{PAD}" y="{y}" font-size="13" font-weight="600" class="fg">Failing</text>')
        y += 10
        body.append(f'<line x1="{PAD}" y1="{y + .5}" x2="{right}" y2="{y + .5}" class="rule"/>')
        y += 4
        max_chars = int((right - grid_x) / 7.3)  # 12px monospace is ~7.2px per character
        for r in failed:
            y += 20
            message = r.get("message", "")
            if len(message) > max_chars:
                message = message[:max_chars - 1].rstrip() + "…"
            body.append(f'<text x="{PAD}" y="{y}" font-size="12" class="fg">{escape(labels[r["loader"]])} {escape(r["mcVersion"])}</text>')
            if message:
                body.append(f'<text x="{grid_x}" y="{y}" font-size="12" class="mono muted">{escape(message)}'
                            f'<title>{escape(r["message"])}</title></text>')
        y += 8

    height = y + PAD
    return "\n".join([
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        f"<style>{style()}</style>",
        f'<rect x=".5" y=".5" width="{width - 1}" height="{height - 1}" rx="12" class="card"/>',
        *body,
        "</svg>",
        "",
    ])


def cell(loader, version, x, y, status, result, supported, pending):
    if (loader.id, version) in pending:
        return (f'<rect x="{x + .5}" y="{y + .5}" width="{CELL_W - 1}" height="{CELL_H - 1}" rx="4" class="pending">'
                f'<title>{escape(f"{loader.label} {version}: no {loader.label} build yet")}</title></rect>')
    if (loader.id, version) not in supported:
        return ""

    tooltip = f"{loader.label} {version}: "
    if result:
        tooltip += STATUSES[status]
        if result.get("timestamp"):
            tooltip += f" (since {result['timestamp'][:10]})"
        if result.get("message"):
            tooltip += f"\n{result['message']}"
    else:
        tooltip += "not tested yet"
    return (f'<rect x="{x}" y="{y}" width="{CELL_W}" height="{CELL_H}" rx="4" class="{status}">'
            f'<title>{escape(tooltip)}</title></rect>')


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    commands = parser.add_subparsers(required=True)

    jobs = commands.add_parser("jobs", help="print the GitHub Actions job matrix")
    jobs.add_argument("--loaders", help="comma separated, empty = all")
    jobs.add_argument("--versions", help="comma separated, empty = all")
    jobs.add_argument("--pairs", help="JSON file with [loader, version] pairs, only these get jobs (from discover --pairs-out)")
    jobs.add_argument("--allow-empty", action="store_true", help="print an empty matrix instead of failing")
    jobs.set_defaults(run=cmd_jobs)

    discover = commands.add_parser("discover", help="add new Minecraft releases and new loader builds to versions.toml")
    discover.add_argument("--dry-run", action="store_true", help="only print what would change")
    discover.add_argument("--pairs-out", help="write the newly added [loader, version] pairs to this JSON file")
    discover.set_defaults(run=cmd_discover)

    merge = commands.add_parser("merge", help="merge test result files into results.json")
    merge.add_argument("dir", nargs="?", default=ROOT / "minified-launch/build/matrix-results",
                       help="folder with the result files of VersionSupportTest (default: its local output folder)")
    merge.add_argument("--jobs", help="the job matrix JSON of the run, jobs without a result file count as failed")
    merge.add_argument("--run-url", help="stored with every result that changed")
    merge.set_defaults(run=cmd_merge)

    render_ = commands.add_parser("render", help="draw results.json as SVG")
    render_.add_argument("-o", "--output", default=ROOT / "SUPPORTED_VERSIONS.svg")
    render_.set_defaults(run=cmd_render)

    args = parser.parse_args()
    args.run(args)


if __name__ == "__main__":
    main()
