#!/usr/bin/env python3
"""
Daily Collection Script for agents_kit_ideas brain.

Processes all sources from inspirations/ folder:
- Repos: temp clone, analyze, delete
- Folders: scan changes
- URLs: fetch + summarize
- Prompts: execute research

Usage:
  python scripts/daily_collection.py [--dry-run] [--verbose]
"""

import os
import json
import subprocess
import tempfile
import shutil
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Any
import re

# Configuration
REPO_ROOT = Path(__file__).parent.parent
INSPIRATIONS_DIR = REPO_ROOT / "inspirations"
BRAIN_LOCAL_DIR = REPO_ROOT / ".brain_local"
REPORTS_DIR = REPO_ROOT / "reports"
TIMESTAMP = datetime.now().strftime("%Y-%m-%d")

# Ensure directories exist
BRAIN_LOCAL_DIR.mkdir(exist_ok=True)
REPORTS_DIR.mkdir(exist_ok=True)
(BRAIN_LOCAL_DIR / "last_run").touch(exist_ok=True)


class CollectionRunner:
    """Main collection orchestrator."""

    def __init__(self, dry_run=False, verbose=False):
        self.dry_run = dry_run
        self.verbose = verbose
        self.collections = {
            "repos": [],
            "folders": [],
            "urls": [],
            "research": [],
        }
        self.errors = []
        self.summary = {}

    def log(self, msg, level="info"):
        """Simple logging."""
        if self.verbose or level != "debug":
            prefix = f"[{level.upper()}]"
            print(f"{prefix} {msg}")

    def get_last_run_timestamp(self):
        """Read last run timestamp."""
        last_run_file = BRAIN_LOCAL_DIR / "last_run"
        if last_run_file.exists():
            return last_run_file.read_text().strip()
        return None

    def save_last_run(self):
        """Save current timestamp."""
        (BRAIN_LOCAL_DIR / "last_run").write_text(TIMESTAMP)

    def parse_inspirations(self) -> Dict[str, List[Dict[str, str]]]:
        """Parse all .md files in inspirations/ folder."""
        sources = {"repos": [], "folders": [], "urls": [], "research": []}

        if not INSPIRATIONS_DIR.exists():
            self.log(f"Inspirations folder not found: {INSPIRATIONS_DIR}", "error")
            return sources

        for md_file in INSPIRATIONS_DIR.glob("*.md"):
            if md_file.name == "README.md":
                continue

            content = md_file.read_text()
            self.log(f"Parsing {md_file.name}", "debug")

            # Split by headers to avoid cross-section capture
            sections = re.split(r"^## ", content, flags=re.MULTILINE)[1:]  # Skip header before first ##

            for section in sections:
                lines = section.split("\n", 1)
                if len(lines) < 2:
                    continue

                name = lines[0].strip()
                body = lines[1]

                # Look for source line (backtick enclosed)
                source_match = re.search(r"`(repo|folder|url|prompt):(.+?)`", body)
                if not source_match:
                    continue

                source_type, source_value = source_match.groups()
                source_value = source_value.strip()

                if source_type == "repo":
                    sources["repos"].append({"name": name, "url": source_value, "file": md_file.name})
                elif source_type == "folder":
                    sources["folders"].append({"name": name, "path": source_value, "file": md_file.name})
                elif source_type == "url":
                    sources["urls"].append({"name": name, "url": source_value, "file": md_file.name})
                elif source_type == "prompt":
                    sources["research"].append({"name": name, "prompt": source_value, "file": md_file.name})

        self.log(f"Found: {len(sources['repos'])} repos, {len(sources['folders'])} folders, {len(sources['urls'])} URLs, {len(sources['research'])} research")
        return sources

    def collect_from_repos(self, repos: List[Dict]):
        """Temp clone repos, analyze, delete."""
        for repo_info in repos:
            name = repo_info["name"]
            url = repo_info["url"]
            self.log(f"Processing repo: {name}")

            with tempfile.TemporaryDirectory() as tmpdir:
                try:
                    # Clone
                    clone_path = Path(tmpdir) / "repo"
                    if self.dry_run:
                        self.log(f"  [DRY] Would clone: {url}", "debug")
                    else:
                        subprocess.run(
                            ["git", "clone", "--depth=1", url, str(clone_path)],
                            capture_output=True,
                            timeout=30,
                            check=True,
                        )
                        self.log(f"  Cloned to {clone_path}")

                        # Get recent commits
                        result = subprocess.run(
                            ["git", "-C", str(clone_path), "log", "--oneline", "-10"],
                            capture_output=True,
                            text=True,
                            check=True,
                        )
                        commits = result.stdout.strip().split("\n")

                        # Get file list
                        result = subprocess.run(
                            ["find", str(clone_path), "-type", "f", "-name", "*.md"],
                            capture_output=True,
                            text=True,
                            check=True,
                        )
                        md_files = [p.replace(str(clone_path) + "/", "") for p in result.stdout.strip().split("\n") if p]

                        self.collections["repos"].append({
                            "name": name,
                            "url": url,
                            "recent_commits": commits[:5],
                            "md_files_count": len(md_files),
                            "sample_files": md_files[:10],
                            "timestamp": TIMESTAMP,
                        })
                        self.log(f"  Found {len(md_files)} markdown files")

                except subprocess.TimeoutExpired:
                    self.errors.append(f"Repo {name}: Clone timeout")
                    self.log(f"  ERROR: Clone timeout", "error")
                except subprocess.CalledProcessError as e:
                    self.errors.append(f"Repo {name}: {e.stderr.decode()[:100]}")
                    self.log(f"  ERROR: {str(e)[:100]}", "error")
                except Exception as e:
                    self.errors.append(f"Repo {name}: {str(e)[:100]}")
                    self.log(f"  ERROR: {str(e)}", "error")

    def collect_from_folders(self, folders: List[Dict]):
        """Scan folders for file changes."""
        for folder_info in folders:
            name = folder_info["name"]
            path = folder_info["path"]
            self.log(f"Processing folder: {name}")

            try:
                folder_path = Path(path).expanduser()
                if not folder_path.exists():
                    self.log(f"  Folder not found: {path}", "error")
                    self.errors.append(f"Folder {name}: Path not found")
                    continue

                # Find all files
                files = list(folder_path.rglob("*"))
                md_files = [f for f in files if f.suffix == ".md"]
                code_files = [f for f in files if f.suffix in [".py", ".ts", ".js"]]

                self.collections["folders"].append({
                    "name": name,
                    "path": path,
                    "total_files": len(files),
                    "md_files": len(md_files),
                    "code_files": len(code_files),
                    "timestamp": TIMESTAMP,
                })
                self.log(f"  Found {len(md_files)} docs, {len(code_files)} code files")

            except Exception as e:
                self.errors.append(f"Folder {name}: {str(e)[:100]}")
                self.log(f"  ERROR: {str(e)}", "error")

    def collect_from_urls(self, urls: List[Dict]):
        """Fetch and summarize URLs."""
        for url_info in urls:
            name = url_info["name"]
            url = url_info["url"]
            self.log(f"Processing URL: {name}")

            try:
                if self.dry_run:
                    self.log(f"  [DRY] Would fetch: {url}", "debug")
                    self.collections["urls"].append({
                        "name": name,
                        "url": url,
                        "status": "dry_run",
                        "timestamp": TIMESTAMP,
                    })
                else:
                    # Simple HEAD request to check if accessible
                    result = subprocess.run(
                        ["curl", "-sI", "--max-time", "5", url],
                        capture_output=True,
                        text=True,
                        timeout=10,
                    )

                    status_line = result.stdout.split("\n")[0] if result.stdout else "unknown"

                    self.collections["urls"].append({
                        "name": name,
                        "url": url,
                        "status": status_line,
                        "timestamp": TIMESTAMP,
                        "note": "Full summarization in phase 2",
                    })
                    self.log(f"  Status: {status_line}")

            except Exception as e:
                self.errors.append(f"URL {name}: {str(e)[:100]}")
                self.log(f"  ERROR: {str(e)}", "error")

    def collect_research_tasks(self, research: List[Dict]):
        """Log research tasks for manual execution."""
        for task_info in research:
            name = task_info["name"]
            prompt = task_info["prompt"]
            self.log(f"Research task: {name}")

            self.collections["research"].append({
                "name": name,
                "prompt": prompt,
                "status": "pending",
                "timestamp": TIMESTAMP,
                "note": "Execute manually or schedule separately",
            })

    def write_cloud_collection(self):
        """Write raw collection to .brain_local/collections/"""
        collections_dir = BRAIN_LOCAL_DIR / "collections"
        collections_dir.mkdir(exist_ok=True)

        collection_file = collections_dir / f"raw_collection_{TIMESTAMP}.json"
        collection_file.write_text(json.dumps(self.collections, indent=2))
        self.log(f"Saved raw collection: {collection_file}")

    def write_summary(self):
        """Generate and write human-readable summary."""
        summary_lines = [
            f"# Daily Collection — {TIMESTAMP}",
            "",
            "## Summary",
            "",
            f"- Repos processed: {len(self.collections['repos'])}",
            f"- Folders scanned: {len(self.collections['folders'])}",
            f"- URLs checked: {len(self.collections['urls'])}",
            f"- Research tasks: {len(self.collections['research'])}",
            f"- Errors: {len(self.errors)}",
            "",
        ]

        if self.collections["repos"]:
            summary_lines.extend(["## Repos", "", "| Name | Files | Status |", "|------|-------|--------|"])
            for repo in self.collections["repos"]:
                summary_lines.append(f"| {repo['name']} | {repo['md_files_count']} | ✓ |")
            summary_lines.append("")

        if self.collections["folders"]:
            summary_lines.extend(["## Folders", "", "| Name | Docs | Code |", "|------|------|------|"])
            for folder in self.collections["folders"]:
                summary_lines.append(f"| {folder['name']} | {folder['md_files']} | {folder['code_files']} |")
            summary_lines.append("")

        if self.collections["urls"]:
            summary_lines.extend(["## URLs", "", "| Name | Status |", "|------|--------|"])
            for url in self.collections["urls"]:
                status = url["status"].split()[0] if url["status"] != "dry_run" else "pending"
                summary_lines.append(f"| {url['name']} | {status} |")
            summary_lines.append("")

        if self.collections["research"]:
            summary_lines.extend(["## Research Tasks", "", "| Name | Status |", "|------|--------|"])
            for task in self.collections["research"]:
                summary_lines.append(f"| {task['name']} | {task['status']} |")
            summary_lines.append("")

        if self.errors:
            summary_lines.extend(["## Errors", ""])
            for error in self.errors:
                summary_lines.append(f"- {error}")
            summary_lines.append("")

        summary_lines.extend([
            "---",
            "",
            "Raw data: `.brain_local/collections/raw_collection_<date>.json`",
            "",
            "Next phase: Classification and synthesis",
        ])

        summary_text = "\n".join(summary_lines)

        # Write to reports/
        report_file = REPORTS_DIR / f"collection_{TIMESTAMP}.md"
        report_file.write_text(summary_text)
        self.log(f"Saved summary: {report_file}")

        return summary_text

    def run(self):
        """Execute full collection cycle."""
        self.log("Starting daily collection...")

        # Parse sources
        sources = self.parse_inspirations()

        # Collect from each source type
        self.collect_from_repos(sources["repos"])
        self.collect_from_folders(sources["folders"])
        self.collect_from_urls(sources["urls"])
        self.collect_research_tasks(sources["research"])

        # Save results
        self.write_cloud_collection()
        summary = self.write_summary()

        # Update state
        if not self.dry_run:
            self.save_last_run()

        self.log("Collection complete.")
        return summary


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true", help="Don't modify anything")
    parser.add_argument("--verbose", action="store_true", help="Verbose logging")
    args = parser.parse_args()

    runner = CollectionRunner(dry_run=args.dry_run, verbose=args.verbose)
    summary = runner.run()
    print("\n" + summary)
