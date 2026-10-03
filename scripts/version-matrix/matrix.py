#!/usr/bin/env python3
"""
matrix.py jobs   [--loaders a,b] [--versions x,y]   print the GitHub Actions job matrix as JSON
matrix.py merge  [DIR]                              merge test result files into results.json
matrix.py render [-o FILE]                          draw results.json as SVG
"""
import argparse
import json
import sys
import tomllib
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

    loaders = [Loader(id, spec.get("label", id), expand(id, spec["versions"])) for id, spec in raw["loader"].items()]
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

def split(arg):
    return [s.strip() for s in (arg or "").split(",") if s.strip()]


def cmd_jobs(args):
    config = load_config()
    loaders, versions = split(args.loaders), split(args.versions)
    for name, wanted, known in (("loader", loaders, [l.id for l in config.loaders]), ("version", versions, config.versions)):
        if unknown := [w for w in wanted if w not in known]:
            sys.exit(f"unknown {name}: {', '.join(unknown)} (see {CONFIG.name})")

    jobs = [{"loader": l, "mc_version": v} for l, v in config.pairs()
            if (not loaders or l in loaders) and (not versions or v in versions)]
    if not jobs:
        sys.exit("no loader supports any of these versions")
    print(json.dumps({"include": jobs}, separators=(",", ":")))


def cmd_merge(args):
    config = load_config()
    results = load_results()
    expected = set(config.pairs())
    files = sorted(Path(args.dir).rglob("*.json")) if Path(args.dir).is_dir() else []
    if not files:
        print(f"no results in {args.dir}, nothing to merge")

    changed = 0
    for file in files:
        try:
            r = json.loads(file.read_text())
            key = (r["loader"], r["mcVersion"])
            status = r["status"]
        except (json.JSONDecodeError, KeyError, TypeError) as e:
            print(f"skipping {file.name}: {e!r}")
            continue
        if key not in expected:
            print(f"skipping {file.name}: {key[0]} {key[1]} isn't in {CONFIG.name}")
            continue
        if status not in STATUSES:
            print(f"skipping {file.name}: unknown status {status!r}")
            continue
        old = results.get(key)
        if old and old["status"] == status and old.get("message") == r.get("message"):
            continue
        results[key] = {k: r.get(k, "") for k in ("loader", "mcVersion", "status", "message", "timestamp")}
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
        return "".join(rules)

    return (f"text{{font-family:{FONT}}}.mono{{font-family:{MONO}}}"
            + colors(THEMES["light"])
            + f"@media (prefers-color-scheme:dark){{{colors(THEMES['dark'])}}}")


def render(config, results):
    labels = {l.id: l.label for l in config.loaders}
    supported = set(config.pairs())
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
    spans = "".join(f'<tspan dx="{0 if i == 0 else 16}" class="{cls}" font-weight="600">{count}</tspan>'
                    f'<tspan dx="4" class="muted">{escape(text)}</tspan>' for i, (cls, count, text) in enumerate(legend))
    body.append(f'<text x="{right}" y="{y + 22}" font-size="13" text-anchor="end">{spans}</text>')
    y += 48

    # one block per group
    for group in config.groups:
        for block_no, block in enumerate(chunks(group.versions, cols)):
            loaders = [l for l in config.loaders if any((l.id, v) in supported for v in block)]
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
                    body.append(cell(loader, v, x, y, status_of(loader.id, v), results.get((loader.id, v)), supported))
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


def cell(loader, version, x, y, status, result, supported):
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
    jobs.set_defaults(run=cmd_jobs)

    merge = commands.add_parser("merge", help="merge test result files into results.json")
    merge.add_argument("dir", nargs="?", default=ROOT / "minified-launch/build/matrix-results",
                       help="folder with the result files of VersionSupportTest (default: its local output folder)")
    merge.set_defaults(run=cmd_merge)

    render_ = commands.add_parser("render", help="draw results.json as SVG")
    render_.add_argument("-o", "--output", default=ROOT / "SUPPORTED_VERSIONS.svg")
    render_.set_defaults(run=cmd_render)

    args = parser.parse_args()
    args.run(args)


if __name__ == "__main__":
    main()
