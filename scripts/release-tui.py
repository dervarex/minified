#!/usr/bin/env python3
# release-tui.py <current version> <highest released version> <output file>

import curses
import os
import sys

BUTTON4 = getattr(curses, "BUTTON4_PRESSED", 0x80000)
BUTTON5 = getattr(curses, "BUTTON5_PRESSED", 0x200000)


def parse(version):
    return [int(p) for p in version.split(".")]


class Screen:
    def __init__(self, current, released):
        self.current = parse(current)
        self.released = parse(released)
        self.base = max(self.current, self.released)
        self.version = self.bump("patch")
        self.tests = True
        self.watch = True
        self.dry = False
        self.rows = [
            ["num0", "num1", "num2"],
            ["major", "minor", "patch"],
            ["tests"],
            ["watch"],
            ["dry"],
            ["continue", "cancel"],
        ]
        self.focus = "continue"
        self.typing = False
        self.regions = {}

    # state
    def bump(self, kind):
        major, minor, patch = self.base
        return {"major": [major + 1, 0, 0], "minor": [major, minor + 1, 0], "patch": [major, minor, patch + 1]}[kind]

    def kind(self):
        for k in ("major", "minor", "patch"):
            if self.version == self.bump(k):
                return k
        return None

    def valid(self):
        return self.version > self.base

    def text(self, v=None):
        return ".".join(str(p) for p in (v or self.version))

    def change(self, index, delta):
        self.version[index] = max(0, self.version[index] + delta)
        self.typing = False

    def type_digit(self, index, digit):
        value = self.version[index] * 10 + digit if self.typing else digit
        self.version[index] = min(value, 9999)
        self.typing = True

    def position(self):
        for r, row in enumerate(self.rows):
            if self.focus in row:
                return r, row.index(self.focus)
        return 0, 0

    def move(self, dr=0, dc=0):
        r, c = self.position()
        r = (r + dr) % len(self.rows)
        row = self.rows[r]
        c = min(c, len(row) - 1) if dr else (c + dc) % len(row)
        self.focus = row[c]
        self.typing = False

    def cycle(self, step):
        flat = [f for row in self.rows for f in row]
        self.focus = flat[(flat.index(self.focus) + step) % len(flat)]
        self.typing = False

    def activate(self, name):
        """Returns "continue"/"cancel" when the screen is done."""
        if name in ("major", "minor", "patch"):
            self.version = self.bump(name)
        elif name == "tests":
            self.tests = not self.tests
        elif name == "watch":
            self.watch = not self.watch
        elif name == "dry":
            self.dry = not self.dry
        elif name == "continue" and self.valid():
            return "continue"
        elif name == "cancel":
            return "cancel"
        return None

    # drawing
    def put(self, win, y, x, s, attr=0, name=None):
        h, w = win.getmaxyx()
        if 0 <= y < h and x < w:
            win.addnstr(y, x, s, max(0, w - x - 1), attr)
        if name:
            self.regions.setdefault(name, []).append((y, x, x + len(s)))
        return x + len(s)

    def focused(self, name):
        return curses.A_REVERSE if self.focus == name else 0

    def draw(self, win):
        win.erase()
        self.regions = {}
        dim = curses.A_DIM
        bold = curses.A_BOLD
        x0, y = 3, 1

        self.put(win, y, x0, "Minified release", bold)
        y += 2
        self.put(win, y, x0, "current ", dim)
        self.put(win, y, x0 + 9, self.text(self.current))
        if self.released != self.current:
            self.put(win, y, x0 + 9 + len(self.text(self.current)) + 2, f"(released: {self.text(self.released)})", dim)
        y += 2

        # version, with clickable arrows above and below each part
        self.put(win, y + 1, x0, "new", dim)
        x = x0 + 9
        for i in range(3):
            name = f"num{i}"
            cell = f" {self.version[i]} "
            width = max(len(cell), 3)
            cell = cell.center(width)
            mid = x + width // 2
            self.put(win, y, mid, "▲", dim, f"up{i}")
            self.put(win, y + 1, x, cell, bold | self.focused(name), name)
            self.put(win, y + 2, mid, "▼", dim, f"down{i}")
            x += width
            if i < 2:
                self.put(win, y + 1, x, ".", bold)
                x += 1
        status_x = x + 3
        if self.valid():
            kind = self.kind()
            label = f"{self.text(self.current)} -> {self.text()}" + (f"  ({kind})" if kind else "")
            self.put(win, y + 1, status_x, label, dim)
        else:
            self.put(win, y + 1, status_x, f"must be newer than {self.text(self.base)}",
                     curses.color_pair(1) | bold)
        y += 4

        x = x0
        for kind in ("major", "minor", "patch"):
            preview = self.text(self.bump(kind))
            attr = self.focused(kind) | (bold if self.kind() == kind else 0)
            x = self.put(win, y, x, f"[ {kind.capitalize()} {preview} ]", attr, kind) + 2
        y += 2

        for name, value, label in (
                ("tests", self.tests, "Run the tests locally first"),
                ("watch", self.watch, "Watch the Release workflow after pushing"),
                ("dry", self.dry, "Dry run, only show what would happen")):
            self.put(win, y, x0, f"[{'x' if value else ' '}] {label}", self.focused(name), name)
            y += 1
        y += 1

        cont = curses.A_DIM if not self.valid() else bold
        x = self.put(win, y, x0, "[ Continue ]", cont | self.focused("continue"), "continue") + 2
        self.put(win, y, x, "[ Cancel ]", self.focused("cancel"), "cancel")
        y += 2

        self.put(win, y, x0, "tab/arrows move   ↑↓ or scroll change a number   0-9 type it", dim)
        self.put(win, y + 1, x0, "m/i/p major/minor/patch   space toggle   enter select   q quit", dim)
        win.refresh()

    def hit(self, my, mx):
        for name, spots in self.regions.items():
            for y, a, b in spots:
                if y == my and a <= mx < b:
                    return name
        return None

    # loop
    def run(self, win):
        curses.curs_set(0)
        curses.use_default_colors()
        curses.init_pair(1, curses.COLOR_RED, -1)
        curses.mousemask(curses.ALL_MOUSE_EVENTS | curses.REPORT_MOUSE_POSITION)
        curses.mouseinterval(0)
        win.keypad(True)
        while True:
            self.draw(win)
            key = win.get_wch()
            result = self.handle(key)
            if result:
                return result

    def handle(self, key):
        digit = self.focus[-1] if self.focus.startswith("num") else None
        if key == curses.KEY_MOUSE:
            return self.mouse()
        if key in ("q", "\x1b"):
            return "cancel"
        if key in ("\t",):
            self.cycle(1)
        elif key == curses.KEY_BTAB:
            self.cycle(-1)
        elif key in ("m", "i", "p"):
            self.version = self.bump({"m": "major", "i": "minor", "p": "patch"}[key])
        elif key in (curses.KEY_UP, "k"):
            if digit:
                self.change(int(digit), 1)
            else:
                self.move(dr=-1)
        elif key in (curses.KEY_DOWN, "j"):
            if digit:
                self.change(int(digit), -1)
            else:
                self.move(dr=1)
        elif key in (curses.KEY_LEFT, "h"):
            self.move(dc=-1)
        elif key in (curses.KEY_RIGHT, "l", "."):
            self.move(dc=1)
        elif isinstance(key, str) and key.isdigit() and digit:
            self.type_digit(int(digit), int(key))
        elif key in (curses.KEY_BACKSPACE, "\x7f", "\b") and digit:
            self.version[int(digit)] //= 10
            self.typing = True
        elif key in ("\n", "\r", curses.KEY_ENTER):
            if digit:
                self.focus = "continue"
            else:
                return self.activate(self.focus)
        elif key == " ":
            if not digit:
                return self.activate(self.focus)
        return None

    def mouse(self):
        try:
            _, mx, my, _, state = curses.getmouse()
        except curses.error:
            return None
        name = self.hit(my, mx)
        if name is None:
            return None
        if state & (BUTTON4 | BUTTON5):
            index = int(name[-1]) if name[:-1] in ("num", "up", "down") else None
            if index is not None:
                self.change(index, 1 if state & BUTTON4 else -1)
            return None
        if not state & (curses.BUTTON1_PRESSED | curses.BUTTON1_CLICKED):
            # the release that follows a press would act a second time
            return None
        if name.startswith("up"):
            self.change(int(name[-1]), 1)
        elif name.startswith("down"):
            self.change(int(name[-1]), -1)
        elif name.startswith("num"):
            self.focus = name
            self.typing = False
        else:
            self.focus = name
            return self.activate(name)
        return None


def main():
    if len(sys.argv) != 4:
        print(__doc__.strip(), file=sys.stderr)
        return 2
    current, released, out = sys.argv[1:]
    os.environ.setdefault("ESCDELAY", "25")
    screen = Screen(current, released)
    result = curses.wrapper(screen.run)
    if result != "continue":
        return 1
    with open(out, "w") as f:
        f.write(f"{screen.text()} {int(screen.tests)} {int(screen.watch)} {int(screen.dry)}\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
