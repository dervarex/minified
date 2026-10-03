#!/usr/bin/env python3
import json
import sys
from datetime import date
from pathlib import Path
from html import escape

INPUT_DIR   = Path(sys.argv[1] if len(sys.argv) > 1 else "matrix-results")
OUTPUT_FILE = Path(sys.argv[2] if len(sys.argv) > 2 else "SUPPORTED_VERSIONS.svg")
TEMPLATE    = Path(sys.argv[3] if len(sys.argv) > 3 else "template.svg")

if not INPUT_DIR.is_dir():
    sys.exit(f"missing: {INPUT_DIR}")

results = []
for path in INPUT_DIR.glob("*.json"):
    try:
        results.append(json.loads(path.read_text()))
    except json.JSONDecodeError as e:
        print(f"skipping {path.name}: {e}")

loaders  = sorted({r["loader"] for r in results})
versions = sorted({r["mcVersion"] for r in results}, key=version_key)
lookup   = {f"{r['loader']}/{r['mcVersion']}": r["status"] for r in results}
status   = lambda l, v: lookup.get(f"{l}/{v}", "missing")

def version_key(v):
    eras = [r"^rd-", r"^c\d", r"^in(f)?-", r"^a\d", r"^b\d"]
    for i, pat in enumerate(eras):
        if re.match(pat, v):
            return (i, v)
    return (len(eras), v)

sections = [
    ("Pre-1.0",     lambda v: re.match(r"^(rd-|c\d|in(f)?-|a\d|b\d)", v)),
    ("1.7 – 1.13",  lambda v: re.match(r"^1\.(7|8|9|10|11|12|13)\.", v)),
    ("1.14 – 1.21", lambda v: re.match(r"^1\.(14|15|16|17|18|19|20|21)\.", v)),
    ("26.x",        lambda v: re.match(r"^26\.", v)),
]

COLORS = {"pass": "#7ab87a", "fail": "#c26565", "skipped": "#c2a065"}
short  = lambda v: v.split("-")[0].split("_")[0] if len(v) > 7 else v

counts = {"pass": 0, "fail": 0, "skipped": 0, "missing": 0}
for v in versions:
    for l in loaders:
        counts[status(l, v)] += 1

X0, STEP, ROW, MAX_COLS = 200, 60, 30, 14

def dot(s, x, y):
    if s in COLORS:
        return f'<circle cx="{x}" cy="{y}" r="3.5" fill="{COLORS[s]}"/>'
    return f'<circle cx="{x}" cy="{y}" r="3.5" fill="none" stroke="#3a3a42" stroke-width="1.2"/>'

def section(title, match):
    vs = [v for v in versions if match(v)]
    if not vs:
        return [], 0
    rows = -(-len(vs) // MAX_COLS)
    ls = [l for l in loaders if any(status(l, v) != "missing" for v in vs)]
    parts = [f'<text x="58" y="0" font-size="13" font-weight="600" fill="#e8e8ec">{escape(title)}</text>']
    return parts, rows, ls, vs

content_lines = []
y = 128
for title, match in sections:
    vs = [v for v in versions if match(v)]
    if not vs:
        continue
    rows = -(-len(vs) // MAX_COLS)
    ls = [l for l in loaders if any(status(l, v) != "missing" for v in vs)]

    y += 64
    content_lines.append(f'<text x="58" y="{y}" font-size="13" font-weight="600" fill="#e8e8ec">{escape(title)}</text>')

    for r in range(rows):
        chunk = vs[r * MAX_COLS:(r + 1) * MAX_COLS]

        y += 32
        labels = "".join(f'<text x="{X0 + i * STEP}" y="{y}">{escape(short(v))}</text>' for i, v in enumerate(chunk))
        content_lines.append(f'<g font-size="11" fill="#5a5a62" text-anchor="middle" font-family="Consolas, \'Courier New\', monospace">{labels}</g>')

        y += 14
        x_end = X0 + (len(chunk) - 1) * STEP + 30
        content_lines.append(f'<line x1="{X0 - 30}" y1="{y}" x2="{x_end}" y2="{y}" stroke="#1a1a1f" stroke-width="1"/>')

        for l in ls:
            y += ROW
            content_lines.append(f'<text x="176" y="{y + 4}" font-size="13" fill="#c8c8ce" text-anchor="end">{escape(l)}</text>')
            for i, v in enumerate(chunk):
                content_lines.append(dot(status(l, v), X0 + i * STEP, y))

        if r < rows - 1:
            y += 20

    y += 40

footer_top  = y + 20
footer_text = footer_top + 24

summary = (
    f"{counts['pass']} passed · {counts['fail']} failed · "
    f"{counts['skipped']} skipped · {counts['missing']} not tested"
)

out = TEMPLATE.read_text().format(
    height=footer_top + 40,
    date=date.today().isoformat(),
    content="\n".join(content_lines),
    footer_top=footer_top,
    footer_text=footer_text,
    summary=summary,
)

OUTPUT_FILE.write_text(out)
print(f"wrote {OUTPUT_FILE}")