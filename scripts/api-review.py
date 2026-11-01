#!/usr/bin/env python3
# API.Status.EXPERIMENTAL review utility
# Claude helped here, base idea and implementation are made by myself
import math
import os
import re
import shutil
import subprocess
import sys
import tkinter as tk
import tkinter.font as tkfont
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from tkinter import messagebox, ttk
from types import SimpleNamespace

ROOT = Path(__file__).resolve().parent.parent
API = re.compile(r'@API\s*\(([^)]*)\)')
STATUS = re.compile(r'status\s*=\s*(?:[\w.]*\.)?(\w+)')
SINCE = re.compile(r'since\s*=\s*"([^"]*)"')
TOP_TYPE = re.compile(r'^public\s+(?:(?:final|abstract|sealed|non-sealed|static|strictfp)\s+)*'
                      r'(class|interface|enum|record|@interface)\s+(\w+)', re.M)
DECL = re.compile(r'\b(class|interface|enum|record|@interface)\s+(\w+)|(\w+)\s*\(([^)]*)\)|(\w+)\s*[;=]')


@dataclass
class Place:
    problem: str  # "Experimental" or "No status"
    name: str
    kind: str
    module: str
    file: Path
    offset: int  # start of the @API annotation, or of the declaration that lacks one
    line: int
    exported: bool
    since: str = ""
    changed: str = ""


def scan():
    places = []
    modules = re.findall(r"include\s*\(?\s*['\"]:?([^'\"]+)['\"]", (ROOT / "settings.gradle").read_text())
    for module in (m for m in modules if m != "docs"):
        files = {p: p.read_text(encoding="utf-8") for p in sorted((ROOT / module / "src/main/java").rglob("*.java"))}
        info = next((t for p, t in files.items() if p.name == "module-info.java"), None)
        exports = set(re.findall(r'exports\s+([\w.]+)\s*;', info)) if info else None
        marked = {p.parent for p, t in files.items() if p.name == "package-info.java" and API.search(t)}
        for path, text in files.items():
            package = (re.search(r'^package\s+([\w.]+)', text, re.M) or [None, ""])[1]
            exported = exports is None or package in exports

            def add(problem, name, kind, offset, since=""):
                places.append(Place(problem, name, kind, module, path, offset,
                                    text.count("\n", 0, offset) + 1, exported, since))

            for m in API.finditer(text):
                if (STATUS.search(m[1]) or [None, ""])[1] != "EXPERIMENTAL":
                    continue
                since = (SINCE.search(m[1]) or [None, ""])[1]
                if path.name == "package-info.java":
                    add("Experimental", package, "package", m.start(), since)
                    continue
                d = DECL.search(re.sub(r'@\w+(\([^)]*\))?', '', text[m.end():]))
                kind, name = (d[1], d[2]) if d[1] else ("method", f"{d[3]}({d[4].strip()})") if d[3] else ("field", d[5])
                add("Experimental", name if name == path.stem else f"{path.stem}.{name}", kind, m.start(), since)
            for m in TOP_TYPE.finditer(text):
                head = text[:m.start()]  # annotations sit after the last import or javadoc
                if "@API" not in head[max(head.rfind(";"), head.rfind("*/")):] and path.parent not in marked:
                    add("No status", m[2], m[1].lstrip("@"), m.start())
    with ThreadPoolExecutor(8) as pool:
        for p, date in zip(places, pool.map(changed, places)):
            p.changed = date
    return sorted(places, key=lambda p: (p.problem, p.module, p.name))


def changed(p):
    out = subprocess.run(["git", "blame", "--porcelain", "-L", f"{p.line},{p.line}", "--", str(p.file)],
                         cwd=ROOT, capture_output=True, text=True).stdout
    m = re.search(r'^author-time (\d+)', out, re.M)
    return "uncommitted" if out.startswith("0" * 40) else datetime.fromtimestamp(int(m[1])).date().isoformat() if m else ""


def mark(places, status, since):
    """Sets places to status, last place in a file first so the offsets stay valid"""
    for path in {p.file for p in places}:
        text = path.read_text(encoding="utf-8")
        for p in sorted((p for p in places if p.file == path), key=lambda p: -p.offset):
            if p.problem == "Experimental":
                m = API.match(text, p.offset)
                args = STATUS.sub(lambda s: s[0][:s.start(1) - s.start()] + status, m[1], count=1)
                if since:
                    args = SINCE.sub(f'since = "{since}"', args) if SINCE.search(args) else f'{args}, since = "{since}"'
                text = text[:m.start(1)] + args + text[m.end(1):]
            else:
                ann = f"@API(status = API.Status.{status}" + (f', since = "{since}"' if since else "") + ")\n"
                text = text[:p.offset] + ann + text[p.offset:]
        if not re.search(r'^import org\.apiguardian\.api\.(API|\*);', text, re.M):
            last = list(re.finditer(r'^(import|package) .*;$', text, re.M))[-1]
            gap = "\n" if last[1] == "import" else "\n\n"
            text = text[:last.end()] + gap + "import org.apiguardian.api.API;" + text[last.end():]
        path.write_text(text, encoding="utf-8", newline="")


def open_in_idea(p):
    """The idea launcher hands the file to the running IntelliJ, which opens it in the project's window"""
    toolbox = Path.home() / ".local/share/JetBrains/Toolbox/scripts/idea"
    idea = os.environ.get("IDEA") or shutil.which("idea") or (str(toolbox) if toolbox.exists() else None)
    if not idea:
        raise OSError("No IntelliJ launcher found, set IDEA=/path/to/idea")
    subprocess.Popen([idea, "--line", str(p.line), str(p.file)], stdout=subprocess.DEVNULL,
                     stderr=subprocess.DEVNULL, start_new_session=True)


FALLBACK = {"background": "0a0f0f", "surfaceContainer": "131b1a", "surfaceContainerHigh": "192120",
            "onSurface": "dce8e6", "onSurfaceVariant": "a2adac", "primary": "9bd0cc", "onPrimary": "0d4845",
            "secondaryContainer": "27403e", "onSecondaryContainer": "a9c5c2", "outlineVariant": "3f4a49"}
CORNERS = {}


def scheme():
    """Material colors of the rice (caelestia writes them for Hyprland), so the window follows the wallpaper"""
    files = [p for p in (Path.home() / ".config/hypr/scheme.lua", Path.home() / ".local/state/caelestia/scheme.json")
             if p.exists()]
    colors = dict(FALLBACK)
    if files:
        colors.update(re.findall(r'"?(\w+)"?\s*[=:]\s*"([0-9a-fA-F]{6})"', max(files, key=os.path.getmtime).read_text()))
    return {k: "#" + v for k, v in colors.items()}


def mix(a, b, t):
    """a with t of b on top, like Material's state layers"""
    x, y = int(a[1:], 16), int(b[1:], 16)
    return "#" + "".join(f"{round(((x >> s) & 255) * (1 - t) + ((y >> s) & 255) * t):02x}" for s in (16, 8, 0))


def rounded(canvas, x1, y1, x2, y2, r, fill, bg, tag="shape"):
    """Flat rounded rectangle. Tk doesn't antialias shapes, so the corners are images with computed coverage"""
    canvas.create_rectangle(x1 + r, y1, x2 - r, y2, fill=fill, width=0, tags=tag)
    canvas.create_rectangle(x1, y1 + r, x2, y2 - r, fill=fill, width=0, tags=tag)
    for left, top, x, y in ((1, 1, x1, y1), (0, 1, x2 - r, y1), (1, 0, x1, y2 - r), (0, 0, x2 - r, y2 - r)):
        key = (fill, bg, r, left, top)
        if key not in CORNERS:
            def cover(i, j):
                dx, dy = (r - i - 0.5) if left else (i + 0.5), (r - j - 0.5) if top else (j + 0.5)
                return max(0.0, min(1.0, r + 0.5 - math.hypot(dx, dy)))
            CORNERS[key] = tk.PhotoImage(width=r, height=r)
            CORNERS[key].put(" ".join("{" + " ".join(mix(bg, fill, cover(i, j)) for i in range(r)) + "}"
                                      for j in range(r)))
        canvas.create_image(x, y, image=CORNERS[key], anchor="nw", tags=tag)


class Button(tk.Canvas):
    """kind: primary (filled), tonal, or text (filled only while selected). With var it's a toggle (filter chip)"""

    def __init__(self, parent, ui, text, command, kind="tonal", var=None):
        super().__init__(parent, height=36, bg=parent["bg"], highlightthickness=0, bd=0, cursor="hand2")
        self.ui, self.kind, self.command, self.var, self.hover, self.selected = ui, kind, command, var, False, False
        self.min_width = 0
        self.bind("<Enter>", lambda e: self.set(hover=True))
        self.bind("<Leave>", lambda e: self.set(hover=False))
        self.bind("<ButtonRelease-1>", lambda e: (var and var.set(not var.get()), self.set(), self.command()))
        self.set(label=text)

    def set(self, **changes):
        self.__dict__.update(changes)
        c, bg = self.ui.c, self["bg"]
        if self.var:
            self.selected = self.var.get()
        fill, fg = {"primary": (c["primary"], c["onPrimary"]),
                    "tonal": (c["secondaryContainer"], c["onSecondaryContainer"]),
                    "text": (c["secondaryContainer"], c["onSecondaryContainer"]) if self.selected
                    else (bg, c["onSurfaceVariant"])}[self.kind]
        fill = mix(fill, fg, 0.08) if self.hover else fill
        text = ("✓  " if self.selected and self.var else "") + self.label
        w = max(self.ui.font.measure(text) + 32, self.min_width)
        self.configure(width=w)
        self.delete("all")
        if self.var and not self.selected:  # outlined while off
            rounded(self, 0, 0, w, 36, 10, c["outlineVariant"], bg)
            rounded(self, 1, 1, w - 1, 35, 9, fill, c["outlineVariant"])
        elif fill != bg:
            rounded(self, 0, 0, w, 36, 10, fill, bg)
        self.create_text(w / 2, 18, text=text, fill=fg, font=self.ui.font)


def window():
    root = tk.Tk(className="api-review")
    root.title("API review")
    root.geometry("1200x720")
    c = scheme()
    ui = SimpleNamespace(c=c, font=tkfont.Font(family="Rubik", size=10),
                         mono=tkfont.Font(family="JetBrainsMono Nerd Font", size=10))
    root.configure(bg=c["background"])
    card = c["surfaceContainer"]
    style = ttk.Style(root)
    style.theme_use("clam")
    style.layout("Treeview", [("Treeview.treearea", {"sticky": "nswe"})])
    style.configure("Treeview", background=card, fieldbackground=card, foreground=c["onSurface"], rowheight=34,
                    font=ui.mono, borderwidth=0)
    style.map("Treeview", background=[("selected", c["secondaryContainer"])],
              foreground=[("selected", c["onSecondaryContainer"])])
    style.configure("Treeview.Heading", background=card, foreground=c["onSurfaceVariant"], font=ui.font,
                    relief="flat", padding=(4, 8), borderwidth=0, lightcolor=card, darkcolor=card, bordercolor=card)
    style.map("Treeview.Heading", background=[("active", card)], foreground=[("active", c["onSurface"])])
    style.layout("Thin.Vertical.TScrollbar", [("Vertical.Scrollbar.trough", {"sticky": "ns", "children": [
        ("Vertical.Scrollbar.thumb", {"expand": "1", "sticky": "nswe"})]})])
    style.configure("Thin.Vertical.TScrollbar", troughcolor=card, background=c["outlineVariant"], bordercolor=card,
                    lightcolor=c["outlineVariant"], darkcolor=c["outlineVariant"], arrowsize=6, gripcount=0)
    style.map("Thin.Vertical.TScrollbar", background=[("active", c["onSurfaceVariant"])])
    show, query = tk.StringVar(value="Everything"), tk.StringVar()
    set_since = tk.BooleanVar(value=False)
    version = re.search(r"version\s*=\s*'([^']+)'", (ROOT / "build.gradle").read_text())[1]
    state = {"places": [], "rows": [], "stamp": None, "sort": None, "hover": None}

    def stamp():
        return sum(p.stat().st_mtime_ns for p in ROOT.glob("*/src/main/java/**/*.java"))

    def reload():
        state["places"], state["stamp"] = scan(), stamp()
        refill()

    def refill():
        q = query.get().lower()
        rows = [p for p in state["places"] if show.get() in ("Everything", p.problem)
                and q in f"{p.file} {p.name}".lower()]
        if state["sort"]:
            rows.sort(key=lambda p: str(getattr(p, state["sort"])))
        state["rows"] = rows
        table.delete(*table.get_children())
        for i, p in enumerate(rows):
            table.insert("", "end", iid=str(i), values=(p.problem, p.name, p.kind, p.module, p.since, p.changed,
                                                        f"{p.file.name}:{p.line}"))

    def selected():
        return [state["rows"][int(i)] for i in table.selection()]

    def apply(new_status):
        if places := selected():
            last = max(int(i) for i in table.selection())
            after = state["rows"][last + 1] if last + 1 < len(state["rows"]) else None
            mark(places, new_status, f"v{version}" if set_since.get() else None)
            reload()
            # select the row that followed the marked ones; marked rows usually drop out of the list
            key = lambda p: (p.file, p.name, p.kind)
            rows = state["rows"]
            index = next((i for i, p in enumerate(rows) if after and key(p) == key(after)), len(rows) - 1)
            if index >= 0:
                table.selection_set(str(index))
                table.focus(str(index))
                table.see(str(index))
            table.focus_set()

    def jump(_=None):
        try:
            for p in selected()[:1]:
                open_in_idea(p)
        except OSError as e:
            messagebox.showerror("IntelliJ", str(e))

    def filter_by(name):
        show.set(name)
        for b in segments:
            b.set(selected=b.label == name)
        refill()

    def hover(row):
        if state["hover"] and table.exists(state["hover"]):
            table.item(state["hover"], tags=())
        if row:
            table.item(row, tags=("hover",))
        state["hover"] = row

    (top := tk.Frame(root, bg=c["background"])).pack(fill="x", padx=16, pady=(14, 10))
    segments = [Button(top, ui, name, lambda n=name: filter_by(n), "text")
                for name in ("Everything", "Experimental", "No status")]
    search = tk.Canvas(top, width=300, height=36, bg=c["background"], highlightthickness=0, bd=0)
    rounded(search, 0, 0, 300, 36, 10, c["surfaceContainerHigh"], c["background"])
    search.create_text(14, 18, text="Filter", anchor="w", fill=c["onSurfaceVariant"], font=ui.font)
    entry = tk.Entry(search, textvariable=query, bd=0, highlightthickness=0, bg=c["surfaceContainerHigh"],
                     fg=c["onSurface"], insertbackground=c["onSurface"], font=ui.font)
    x = 24 + ui.font.measure("Filter")
    search.create_window(x, 18, window=entry, anchor="w", width=288 - x)
    query.trace_add("write", lambda *a: refill())
    for w in (*segments, search):
        w.pack(side="left", padx=(0, 10))
    segments[0].set(selected=True)

    card = tk.Canvas(root, bg=c["background"], highlightthickness=0, bd=0)
    card.pack(fill="both", expand=True, padx=16)
    inner = tk.Frame(card, bg=c["surfaceContainer"])
    view = card.create_window(12, 8, window=inner, anchor="nw")

    def fit(e):
        card.delete("shape")
        rounded(card, 0, 0, e.width, e.height, 10, c["surfaceContainer"], c["background"])
        card.tag_lower("shape")
        card.itemconfigure(view, width=e.width - 24, height=e.height - 16)

    card.bind("<Configure>", fit)
    columns = {"problem": 110, "name": 420, "kind": 90, "module": 140, "since": 70, "changed": 100, "file": 300}
    table = ttk.Treeview(inner, columns=list(columns), show="headings")
    for col, width in columns.items():
        table.heading(col, text=col.capitalize(), anchor="w", command=lambda k=col: (state.update(sort=k), refill()))
        table.column(col, width=width, stretch=col in ("name", "file"))
    scroll = ttk.Scrollbar(inner, orient="vertical", command=table.yview, style="Thin.Vertical.TScrollbar")
    table.configure(yscrollcommand=scroll.set)
    scroll.pack(side="right", fill="y", pady=8)
    table.pack(fill="both", expand=True)
    table.tag_configure("hover", background=c["surfaceContainerHigh"])
    table.bind("<Motion>", lambda e: hover(table.identify_row(e.y)))
    table.bind("<Leave>", lambda e: hover(None))
    for key in ("<Double-1>", "<Return>"):
        table.bind(key, jump)
    table.bind("m", lambda e: apply("STABLE"))  # on the table only, so typing m in the filter still works

    def popup():
        menu = tk.Toplevel(root, bg=c["surfaceContainerHigh"], padx=6, pady=6)
        menu.overrideredirect(True)
        items = [Button(menu, ui, n, lambda n=n: (menu.destroy(), apply(n)), "text")
                 for n in ("MAINTAINED", "EXPERIMENTAL", "INTERNAL", "DEPRECATED")]
        for b in items:
            b.set(min_width=max(i.winfo_reqwidth() for i in items))
            b.pack()
        menu.update_idletasks()  # opens upwards, the bar is at the bottom
        menu.geometry(f"+{other.winfo_rootx()}+{other.winfo_rooty() - menu.winfo_reqheight() - 6}")
        menu.grab_set()
        menu.bind("<Escape>", lambda e: menu.destroy())
        menu.bind("<Button-1>", lambda e: e.widget is menu and menu.destroy())  # click outside

    (bottom := tk.Frame(root, bg=c["background"])).pack(fill="x", padx=16, pady=(10, 14))
    other = Button(bottom, ui, "Other status  ▾", popup)
    for w in (Button(bottom, ui, "Mark stable", lambda: apply("STABLE"), "primary"), other,
              Button(bottom, ui, f"Set since v{version}", lambda: None, "text", set_since),
              Button(bottom, ui, "Open in IntelliJ", jump, "text")):
        w.pack(side="left", padx=(0, 10))

    root.bind("<F5>", lambda e: reload())
    root.bind("<Alt-m>", lambda e: apply("STABLE"))
    root.bind("<Control-f>", lambda e: entry.focus_set())
    root.bind("<FocusIn>", lambda e: stamp() != state["stamp"] and reload())  # picks up edits made in IntelliJ
    reload()
    root.mainloop()


if __name__ == "__main__":
    if "--list" in sys.argv[1:]:
        for p in scan():
            print(f"{p.problem:<13} {p.name:<55} {p.kind:<10} {p.since or '-':<8} {p.changed:<11} "
                  f"{p.file.relative_to(ROOT)}:{p.line}" + ("" if p.exported else "  (not exported)"))
    else:
        window()
