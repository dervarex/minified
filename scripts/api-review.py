#!/usr/bin/env python3
# API.Status.EXPERIMENTAL review utility
# Claude helped here, base idea and implementation are made by myself
import os
import re
import shutil
import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path

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


def theme(root, ttk):
    """Flat colors like IntelliJ's new UI, dark when the desktop prefers dark"""
    dark = bool(shutil.which("gsettings")) and "dark" in subprocess.run(
        ["gsettings", "get", "org.gnome.desktop.interface", "color-scheme"], capture_output=True, text=True).stdout
    bg, field, fg, muted, line, sel = ("#1e1f22", "#2b2d30", "#dfe1e5", "#868a91", "#393b40", "#2e436e") if dark \
        else ("#f7f8fa", "#ffffff", "#1e1f22", "#6c707e", "#dfe1e5", "#d4e2ff")
    style, font = ttk.Style(root), ("Noto Sans", 10)
    style.theme_use("clam")
    root.configure(background=bg)
    style.configure(".", background=bg, foreground=fg, fieldbackground=field, bordercolor=line, lightcolor=field,
                    darkcolor=field, troughcolor=bg, arrowcolor=muted, font=font, selectbackground=sel,
                    selectforeground=fg, insertcolor=fg, indicatorbackground=field, indicatorforeground="#3574f0")
    style.layout("Treeview", [("Treeview.treearea", {"sticky": "nswe"})])
    style.configure("Treeview", background=field, rowheight=30)
    style.map("Treeview", background=[("selected", sel)], foreground=[("selected", fg)])
    style.configure("Treeview.Heading", background=bg, foreground=muted, relief="flat", padding=(8, 6))
    style.map("Treeview.Heading", background=[("active", bg)])
    style.configure("TButton", background=field, padding=(12, 5), relief="flat")
    style.map("TButton", background=[("active", line)])
    style.configure("Accent.TButton", background="#3574f0", foreground="#ffffff", bordercolor="#3574f0")
    style.map("Accent.TButton", background=[("active", "#2f64d1")])
    style.map("TCombobox", fieldbackground=[("readonly", field)], foreground=[("readonly", fg)])
    style.configure("TEntry", padding=5)
    style.configure("Muted.TLabel", foreground=muted)
    for key, value in (("background", field), ("foreground", fg), ("selectBackground", sel), ("font", font)):
        root.option_add(f"*TCombobox*Listbox.{key}", value)


def window():
    import tkinter as tk
    from tkinter import messagebox, ttk
    root = tk.Tk()
    root.title("API review")
    root.geometry("1200x700")
    theme(root, ttk)
    show, query, exported_only = tk.StringVar(value="Everything"), tk.StringVar(), tk.BooleanVar(value=True)
    choice, set_since, status = tk.StringVar(value="STABLE"), tk.BooleanVar(value=True), tk.StringVar()
    version = re.search(r"version\s*=\s*'([^']+)'", (ROOT / "build.gradle").read_text())[1]
    state = {"places": [], "rows": [], "stamp": None, "sort": None}

    def stamp():
        return sum(p.stat().st_mtime_ns for p in ROOT.glob("*/src/main/java/**/*.java"))

    def reload(message=""):
        state["places"], state["stamp"] = scan(), stamp()
        refill()
        shown = [p.problem for p in state["places"] if p.exported or not exported_only.get()]
        status.set(f"{message}{shown.count('Experimental')} experimental, {shown.count('No status')} without status")

    def refill():
        q = query.get().lower()
        rows = [p for p in state["places"] if show.get() in ("Everything", p.problem)
                and (p.exported or not exported_only.get()) and q in f"{p.file} {p.name}".lower()]
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
            mark(places, new_status, f"v{version}" if set_since.get() else None)
            reload(f"Set {len(places)} to {new_status}  ·  ")

    def jump(_=None):
        try:
            for p in selected()[:1]:
                open_in_idea(p)
        except OSError as e:
            messagebox.showerror("IntelliJ", str(e))

    (top := ttk.Frame(root, padding=(12, 10))).pack(fill="x")
    box = ttk.Combobox(top, textvariable=show, state="readonly", width=12, values=["Everything", "Experimental", "No status"])
    box.bind("<<ComboboxSelected>>", lambda e: refill())
    entry = ttk.Entry(top, textvariable=query, width=30)
    query.trace_add("write", lambda *a: refill())
    for w in (box, entry, ttk.Checkbutton(top, text="Exported packages only", variable=exported_only, command=reload),
              ttk.Button(top, text="Reload (F5)", command=reload)):
        w.pack(side="left", padx=4)

    columns = {"problem": 100, "name": 400, "kind": 80, "module": 130, "since": 70, "changed": 90, "file": 300}
    table = ttk.Treeview(root, columns=list(columns), show="headings")
    for c, width in columns.items():
        table.heading(c, text=c.capitalize(), anchor="w", command=lambda c=c: (state.update(sort=c), refill()))
        table.column(c, width=width, stretch=c in ("name", "file"))
    table.pack(fill="both", expand=True, padx=12)
    for key in ("<Double-1>", "<Return>"):
        table.bind(key, jump)

    (actions := ttk.Frame(root, padding=(12, 10))).pack(fill="x")
    for w in (ttk.Button(actions, text="Mark STABLE (Alt+M)", style="Accent.TButton", command=lambda: apply("STABLE")),
              ttk.Combobox(actions, textvariable=choice, state="readonly", width=13,
                           values=["STABLE", "MAINTAINED", "EXPERIMENTAL", "INTERNAL", "DEPRECATED"]),
              ttk.Button(actions, text="Apply", command=lambda: apply(choice.get())),
              ttk.Checkbutton(actions, text=f'Set since = "v{version}"', variable=set_since),
              ttk.Button(actions, text="Open in IntelliJ (Enter)", command=jump)):
        w.pack(side="left", padx=4)
    ttk.Label(root, textvariable=status, style="Muted.TLabel", padding=(14, 0, 14, 10)).pack(fill="x")

    root.bind("<F5>", lambda e: reload())
    root.bind("<Alt-m>", lambda e: apply("STABLE"))
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
