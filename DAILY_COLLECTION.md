# Daily Collection Script

Unified data gathering from all sources in `inspirations/`.

## Overview

Run once daily. Reads all enabled sources, collects changes/updates, writes to cloud brain.

```
inspirations/ → collect → cloud brain → local report commit
```

**No classification, no synthesis yet.** Pure data gathering.

---

## Script Steps

### 1. Initialize

```bash
TIMESTAMP=$(date +%Y-%m-%d)
BRAIN_CLOUD="<cloud storage path>"  # TBD
BRAIN_LOCAL=".brain_local"          # local sync cache
REPORT_DIR="reports"

mkdir -p "$REPORT_DIR" "$BRAIN_LOCAL"
```

### 2. Read inspirations manifest

Parse `inspirations/` folder:
- `agent-kernel.md` → repos to pull
- `agents-kit-references.md` → external repos
- `external-links.md` → URLs to fetch
- `internet-research.md` → research tasks (on-demand)
- `custom-prompts.md` → ad-hoc tasks (on-demand)

### 3. Collect from repos

For each `repo:` entry:

```bash
cd <repo_path>
git pull
git log --since="$(cat $BRAIN_LOCAL/.last_run)" --name-status --format="" > /tmp/changes.txt
```

Save to:
- **Cloud:** `collections/raw_repos_<TIMESTAMP>.json`
  ```json
  {
    "repo": "agent-kernel",
    "pulled": true,
    "new_commits": 42,
    "changed_files": [
      { "path": "skills/x.md", "status": "M" },
      { "path": "docs/y.md", "status": "A" }
    ]
  }
  ```

### 4. Collect from folders

For each `folder:` entry:

```bash
find <folder_path> -type f -newer $BRAIN_LOCAL/.last_run
```

Save to:
- **Cloud:** `collections/raw_folders_<TIMESTAMP>.json`
  ```json
  {
    "folder": "/Users/than/Projects/refs/",
    "modified_files": [
      "folder/subdir/file.md",
      "folder/newrepo/.gitignore"
    ]
  }
  ```

### 5. Collect from URLs

For each `url:` entry:

```
a. Fetch URL
b. Check if changed since last fetch (ETag, content hash)
c. If changed:
   - Save raw response to cloud
   - Generate summary (Claude API)
d. Record: URL, timestamp, summary
```

Save to:
- **Cloud:** `collections/raw_urls_<TIMESTAMP>.json`
  ```json
  {
    "url": "https://docs.anthropic.com",
    "fetched": "2026-03-25T09:30:00Z",
    "changed": true,
    "summary": "Added new documentation section on streaming responses...",
    "raw_saved_to": "raw/docs_anthropic_2026-03-25.html"
  }
  ```

### 6. Synthesis report (human summary)

Aggregate all collections into one readable summary:

**Save to:**
- **Cloud:** `collections/collection_<TIMESTAMP>.md`
- **Local commit:** `reports/collection_<TIMESTAMP>.md` (summary only)

**Format:**

```markdown
# Daily Collection — 2026-03-25

## Summary

- Repos checked: 3
- Changed files: 12
- URLs fetched: 4 (1 changed)
- New research tasks: 0

## Details

### Repos

#### agent-kernel
- New commits: 6
- Changed files:
  - `skills/feedback-loop.md` (added)
  - `docs/orchestration.md` (modified)

#### anthropic-sdk
- New commits: 1 (v1.5.1 release)
- Changed files:
  - `examples/mcp_server.py` (modified)

### Folders

#### agents-kit-references (local scan)
- Modified files: 3
- New files: 1

### URLs

#### https://docs.anthropic.com
- Status: **CHANGED** (vs 2026-03-24)
- New: Added section "Streaming with Vision"
- Raw content: `raw/docs_anthropic_2026-03-25.html`

#### https://changelog.anthropic.com
- Status: no change

## Cloud storage

All raw data saved to: `<BRAIN_CLOUD>/collections/`

Files:
- `raw_repos_2026-03-25.json`
- `raw_urls_2026-03-25.json`
- `raw_folders_2026-03-25.json`

---

Next phase: classification and synthesis (phase 2)
```

### 7. Update state

```bash
date +%Y-%m-%d > $BRAIN_LOCAL/.last_run
date +%Y-%m-%d > $BRAIN_CLOUD/.last_run
```

---

## Research tasks (on-demand)

Internet research and custom prompts are **not** part of the daily run.

To execute a research task:

```bash
./scripts/research.sh "<prompt text>"
```

This:
1. Passes prompt to Claude API
2. Returns web search + summary
3. Saves to `collections/research_<TIMESTAMP>.json`
4. Updates `internet-research.md` with timestamp

---

## Cloud brain structure

```
cloud-brain/
├── .meta/
│   ├── .last_run (date)
│   └── state.json (cross-cycle state)
├── collections/
│   ├── collection_2026-03-25.md      (human summary)
│   ├── raw_repos_2026-03-25.json     (git changes)
│   ├── raw_urls_2026-03-25.json      (URL diffs)
│   ├── raw_folders_2026-03-25.json   (filesystem changes)
│   └── research_2026-03-25.json      (on-demand)
└── raw/                               (large files, seldom accessed)
    ├── docs_anthropic_2026-03-25.html
    └── [...]
```

---

## Implementation notes

### API keys

Sources that need credentials:
- `repo:` entries — may need GitHub token (if private)
- `url:` entries with auth — store in `$BRAIN_CLOUD/.secrets` (never committed)
- Claude API calls (for summaries + research) — use `ANTHROPIC_API_KEY`

### Error handling

If a source fails:
- Log error to `collections/errors_<TIMESTAMP>.txt`
- Continue processing other sources
- Report in summary: "agent-kernel: timeout (no new data this cycle)"

### Incremental collection

Use `.last_run` timestamp for all sources:
- Git: `--since="$last_run"`
- Folders: files newer than `$last_run`
- URLs: ETag/content hash comparison
- Never re-fetch everything (saves bandwidth, faster runs)

---

## Scheduling

This script should run:
- **Daily** at a fixed time (e.g., 9 AM UTC)
- Via GitHub Actions (if using cloud repo) or local cron
- Result: new `collection_<YYYY-MM-DD>.md` file each day

Example cron:
```
0 9 * * * /path/to/daily-collection.sh
```

Example GitHub Actions:
```yaml
name: Daily Collection
on:
  schedule:
    - cron: '0 9 * * *'
jobs:
  collect:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run daily collection
        run: python scripts/daily_collection.py
```
