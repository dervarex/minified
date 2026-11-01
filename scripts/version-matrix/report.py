#!/usr/bin/env python3
"""
Keeps one GitHub Issue up to date with the Version Matrix Failures

report.py [--dry-run] [--logs DIR] [--discover-outcome OUTCOME]

- something fails and there is no open issue: opens one
- the issue exists: rewrites its body and comments
- nothing fails anymore: comments and closes the issue

Needs gh with GH_TOKEN and the GITHUB_* variables of a workflow run. --dry-run prints the issue instead,
with no GitHub variables it also skips the GitHub API

Written by Claude under my direction
"""
import argparse
import json
import os
import re
import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import matrix  # noqa: E402

LABEL = "version matrix"
LABEL_COLOR = "5319e7"
MARKER = "<!-- version-matrix-report -->"
FAILING_MARKER = re.compile(r"<!-- failing: (.*?) -->")
MAX_BODY = 60_000         # GitHub allows 65536 characters
EXCERPT_LINES = 60
EXCERPT_LINE_CHARS = 300
ANSI = re.compile(r"\x1b\[[0-9;]*[A-Za-z]")
GRADLE_FOOTER = re.compile(r"^(FAILURE: |BUILD FAILED|BUILD SUCCESSFUL|\d+ tests? completed|> Task \S+ FAILED|VersionSupportTest > \S+ FAILED)")
EXCEPTION = re.compile(r"\b[\w.$]*(Exception|Error)\b")


# GitHub

class GitHub:
    def __init__(self, dry_run):
        self.dry_run = dry_run
        self.repo = os.environ.get("GITHUB_REPOSITORY")
        self.server = os.environ.get("GITHUB_SERVER_URL", "https://github.com")
        self.run_id = os.environ.get("GITHUB_RUN_ID")
        self.offline = not (self.repo and self.run_id)
        if self.offline and not dry_run:
            sys.exit("GITHUB_REPOSITORY and GITHUB_RUN_ID are missing, use --dry-run outside of GitHub Actions")

    @property
    def run_url(self):
        return None if self.offline else f"{self.server}/{self.repo}/actions/runs/{self.run_id}"

    def gh(self, *args, input=None):
        result = subprocess.run(["gh", *args], input=input, capture_output=True, text=True)
        if result.returncode != 0:
            raise RuntimeError(f"gh {' '.join(args)} failed: {result.stderr.strip()}")
        return result.stdout

    def api_lines(self, path, jq):
        if self.offline:
            return []
        out = self.gh("api", "--paginate", path, "--jq", jq)
        return [json.loads(line) for line in out.splitlines() if line.strip()]

    def jobs(self):
        return self.api_lines(f"repos/{self.repo}/actions/runs/{self.run_id}/jobs?per_page=100",
                              ".jobs[] | {name, conclusion, html_url}")

    def artifacts(self):
        return self.api_lines(f"repos/{self.repo}/actions/runs/{self.run_id}/artifacts?per_page=100",
                              ".artifacts[] | {name, id}")

    def artifact_url(self, artifact_id):
        return f"{self.run_url}/artifacts/{artifact_id}"

    def find_issue(self):
        if self.offline:
            return None
        out = self.gh("issue", "list", "--label", LABEL, "--state", "open", "--limit", "50",
                      "--json", "number,body", "--repo", self.repo)
        return next((i for i in json.loads(out) if MARKER in i["body"]), None)

    def ensure_label(self):
        try:
            self.gh("api", f"repos/{self.repo}/labels/{LABEL.replace(' ', '%20')}")
        except RuntimeError:
            self.gh("label", "create", LABEL, "--color", LABEL_COLOR,
                    "--description", "Failures found by the version matrix workflow", "--repo", self.repo)


# Collecting failures

def excerpt(log):
    """The interesting part of a test's output: the crash report, else the last exception, else the end"""
    lines = [ANSI.sub("", l).rstrip() for l in log.splitlines()]
    end = next((i for i, l in enumerate(lines) if GRADLE_FOOTER.match(l.strip())), len(lines))
    lines = lines[:end]

    crash = next((i for i, l in enumerate(lines) if "---- Minecraft Crash Report ----" in l), None)
    if crash is not None:
        start = crash
    else:
        last = next((i for i in range(len(lines) - 1, -1, -1)
                     if EXCEPTION.search(lines[i]) and not lines[i].lstrip().startswith(("at ", "..."))), None)
        if last is not None:
            # walk back to the first exception of that block, "Caused by" chains belong to it
            while last > 0 and (lines[last - 1].lstrip().startswith(("at ", "...", "Caused by", "Suppressed"))
                                or EXCEPTION.search(lines[last - 1])):
                last -= 1
            start = max(0, last - 10)
        else:
            start = max(0, len(lines) - EXCERPT_LINES)

    picked = lines[start:start + EXCERPT_LINES]
    # Gradle indents the test's output by 4 spaces
    indent = min((len(l) - len(l.lstrip()) for l in picked if l.strip()), default=0)
    picked = [l[indent:] for l in picked]
    picked = [l if len(l) <= EXCERPT_LINE_CHARS else l[:EXCERPT_LINE_CHARS - 1] + "…" for l in picked]
    return "\n".join(picked).strip("\n")


def collect(config, github, logs_dir, discover_outcome):
    labels = {l.id: l.label for l in config.loaders}
    results = matrix.load_results()
    jobs = github.jobs()
    job_urls = {j["name"]: j["html_url"] for j in jobs}
    artifacts = {a["name"]: a["id"] for a in github.artifacts()}

    pairs = []
    for loader, version in config.pairs():
        r = results.get((loader, version))
        if not r or r["status"] != "fail":
            continue
        log_file = Path(logs_dir) / f"{loader}-{version}.log" if logs_dir else None
        tested_now = f"{loader} {version}" in job_urls
        artifact = artifacts.get(f"matrix-result-{loader}-{version}")
        pairs.append({
            "key": f"{labels[loader]} {version}",
            "loader": loader,
            "label": labels[loader],
            "version": version,
            "message": r.get("message", ""),
            "since": r.get("timestamp", "")[:10],
            "since_run": r.get("run", ""),
            "job_url": job_urls.get(f"{loader} {version}") if tested_now else None,
            "log_url": github.artifact_url(artifact) if tested_now and artifact and log_file and log_file.exists() else None,
            "excerpt": excerpt(log_file.read_text(errors="replace")) if log_file and log_file.exists() else None,
        })

    # failed jobs that aren't a test, a failing test's job is already covered above
    matrix_jobs = {f"{l} {v}" for l, v in config.pairs()}
    other = [{"key": j["name"], "name": j["name"], "url": j["html_url"], "note": "failed"}
             for j in jobs if j["conclusion"] == "failure" and j["name"] not in matrix_jobs]
    if discover_outcome == "failure":
        prepare = next((j for j in jobs if j["name"] == "Build matrix"), None)
        other.append({"key": "Looking for new versions", "name": "Looking for new Minecraft versions and loader builds",
                      "url": prepare and prepare["html_url"],
                      "note": "failed, new versions weren't added this time"})
    return pairs, other


# Writing the issue

def cell(text, limit=120):
    text = " ".join(text.split())
    if len(text) > limit:
        text = text[:limit - 1] + "…"
    return text.replace("|", "\\|").replace("`", "'")


def link(text, url):
    return f"[{text}]({url})" if url else text


def pair_section(p, with_excerpt):
    reproduce = (f"./gradlew :minified-launch:test --tests 'com.dervarex.minified.launch.VersionSupportTest' "
                 f"-Dgroups=version-matrix -Dmatrix.loader={p['loader']} -Dmatrix.mcVersion={p['version']}")
    links = [f"failing since {link(p['since'] or 'unknown', p['since_run'])}"]
    if p["job_url"]:
        links.append(link("job log", p["job_url"]))
    if p["log_url"]:
        links.append(link("full output", p["log_url"]))
    if not p["job_url"]:
        links.append("not tested in the latest run")

    out = [f"### {p['label']} {p['version']}", "", "```text", p["message"] or "(no message)", "```", "",
           " · ".join(links)]
    if with_excerpt and p["excerpt"]:
        out += ["", "<details><summary>Output excerpt</summary>", "", "```text", p["excerpt"], "```", "", "</details>"]
    out += ["", "<details><summary>Reproduce locally</summary>", "", "```sh", reproduce, "```", "", "</details>", ""]
    return out


def body(config, github, pairs, other, with_excerpts=True):
    keys = sorted([p["key"] for p in pairs] + [o["key"] for o in other])
    run = link("this run", github.run_url)
    out = [MARKER, f"<!-- failing: {json.dumps(keys)} -->", "",
           f"The version matrix has failures, last checked in {run}. This issue gets updated by every run and "
           f"closed once everything passes again.", ""]

    if pairs:
        out += ["| Loader | Minecraft | Failing since | Error |", "|---|---|---|---|"]
        out += [f"| {p['label']} | {p['version']} | {p['since'] or '?'} | {cell(p['message'])} |" for p in pairs]
        out.append("")
    if other:
        out += ["## Failed jobs", ""]
        out += [f"- {link(o['name'], o['url'])} {o['note']}" for o in other]
        out.append("")
    if pairs:
        out += ["## Details", ""]
        for p in pairs:
            out += pair_section(p, with_excerpts)

    waiting = [f"- {l.label}: {', '.join(l.pending)}" for l in config.loaders if l.pending]
    if waiting:
        out += ["## Waiting for loader builds", "", "Will get tested once the loader has a build.", "", *waiting, ""]

    out += ["---", "Tested on ubuntu-latest with Temurin 21, Xvfb and Mesa software rendering, see "
            "`.github/workflows/version-matrix.yml`"]
    return "\n".join(out) + "\n", keys


def fit(config, github, pairs, other):
    text, keys = body(config, github, pairs, other)
    if len(text) <= MAX_BODY:
        return text, keys
    # too long: drop excerpts from the last failures first
    for p in reversed(pairs):
        p["excerpt"] = None
        text, keys = body(config, github, pairs, other)
        if len(text) <= MAX_BODY:
            return text, keys
    return body(config, github, pairs, other, with_excerpts=False)


def title(pairs, other):
    count = len(pairs) + len(other)
    return f"Version matrix: {count} failure{'s' if count != 1 else ''}"


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--dry-run", action="store_true", help="print the issue instead of writing it")
    parser.add_argument("--logs", help="folder with <loader>-<version>.log files of failed tests")
    parser.add_argument("--discover-outcome", default="", help="outcome of the discover step")
    args = parser.parse_args()

    config = matrix.load_config()
    github = GitHub(args.dry_run)
    pairs, other = collect(config, github, args.logs, args.discover_outcome)
    issue = github.find_issue()

    if not pairs and not other:
        if args.dry_run:
            print(f"everything passes{', would close #' + str(issue['number']) if issue else ''}")
        elif issue:
            github.gh("issue", "close", str(issue["number"]), "--repo", github.repo,
                      "--comment", f"Everything passes again in {link('this run', github.run_url)}.")
            print(f"closed #{issue['number']}")
        else:
            print("everything passes")
        return

    text, keys = fit(config, github, pairs, other)
    if args.dry_run:
        print(f"# {title(pairs, other)}\n\n{text}")
        return

    if not issue:
        github.ensure_label()
        url = github.gh("issue", "create", "--title", title(pairs, other), "--body-file", "-",
                        "--label", LABEL, "--label", "bug", "--repo", github.repo, input=text)
        print(f"opened {url.strip()}")
        return

    number = str(issue["number"])
    github.gh("issue", "edit", number, "--title", title(pairs, other), "--body-file", "-", "--repo", github.repo, input=text)
    old = FAILING_MARKER.search(issue["body"])
    old_keys = json.loads(old.group(1)) if old else []
    new, fixed = [k for k in keys if k not in old_keys], [k for k in old_keys if k not in keys]
    if new or fixed:
        lines = [f"Changes in {link('this run', github.run_url)}:", ""]
        lines += [f"- now failing: {k}" for k in new]
        lines += [f"- fixed: {k}" for k in fixed]
        github.gh("issue", "comment", number, "--body-file", "-", "--repo", github.repo, input="\n".join(lines) + "\n")
    print(f"updated #{number}")


if __name__ == "__main__":
    main()
