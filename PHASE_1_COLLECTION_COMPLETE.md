# Phase 1: Data Collection — Complete

## What we built

A unified daily collection system that processes all idea sources into a working brain.

### Architecture

```
inspirations/ → daily_collection.py → .brain_local/ (raw) + reports/ (summaries)
```

### Components

1. **`inspirations/` folder** — Manifest of all sources
   - `agent-kernel.md` — Internal repo contributions
   - `agents-kit-references.md` — External curated projects
   - `external-links.md` — URLs to monitor
   - `internet-research.md` — Research directions
   - `custom-prompts.md` — Ad-hoc research tasks

2. **`scripts/daily_collection.py`** — Unified collection engine
   - Parses inspirations manifest
   - **Repos**: Temp clone → analyze → delete
   - **Folders**: Scan file changes
   - **URLs**: Fetch + check status
   - **Prompts**: Log as research tasks
   - Outputs: Raw JSON + human summaries

3. **`reports/` folder** — Daily summaries (committed)
   - `collection_<YYYY-MM-DD>.md` each day
   - Human-readable overview of what was collected

4. **`.brain_local/` folder** — Raw collection data (gitignored)
   - `collections/raw_collection_<YYYY-MM-DD>.json`
   - `last_run` timestamp for incremental processing

5. **`SCHEDULE_SETUP.md`** — How to automate daily runs
   - Claude Code scheduled task
   - Local cron
   - GitHub Actions (if on GitHub)

### What happens daily

```
1. Parse inspirations/ for all enabled sources
2. For each repo:
   - Clone to temp directory
   - Extract: recent commits, markdown files
   - Delete temp directory
3. For each folder:
   - Scan file counts
   - Detect changes since last run
4. For each URL:
   - Fetch headers
   - Check status / timestamp
5. For each research prompt:
   - Log as pending task
6. Write results:
   - Raw JSON → .brain_local/collections/
   - Summary → reports/
   - Update .last_run timestamp
```

---

## Status

✅ Collection pipeline built and tested
✅ Inspirations manifest populated with examples
✅ Daily collection script working
✅ Scheduling documentation ready

⏳ Schedule setup (waiting for Claude Code schedule skill availability)

---

## Next steps (Phase 2)

1. **Set up daily schedule** (via `/schedule` or local cron)
   - See: `SCHEDULE_SETUP.md`

2. **Populate real sources** in `inspirations/`
   - Update agent-kernel URL
   - Add real external reference repos
   - Add monitoring URLs with API keys
   - Add research directions

3. **Enhance URL collection**
   - Implement content summarization (Claude API)
   - Track page changes via content hash
   - Extract structured data (changelog entries, releases)

4. **Add classification layer**
   - Process raw collections
   - Assign categories (skill, knowledge, structure, etc.)
   - Generate category summaries
   - Track proposed categories (3/3 graduation rule)

5. **Build synthesis engine**
   - Index all ideas with metadata
   - Detect connections (shared category, same problem space, fills gap, pattern repetition)
   - Create compound ideas
   - Implement waiting pool (14-day rule)
   - Generate synthesis summaries

---

## File structure (current)

```
agents_kit_ideas/
├── BRAIN_COLLECTION_V2.md          ← Overview of v2 architecture
├── DAILY_COLLECTION.md             ← Script specification
├── SCHEDULE_SETUP.md               ← Scheduling instructions
├── PHASE_1_COLLECTION_COMPLETE.md  ← This file
├── inspirations/
│   ├── README.md
│   ├── agent-kernel.md
│   ├── agents-kit-references.md
│   ├── custom-prompts.md
│   ├── external-links.md
│   └── internet-research.md
├── scripts/
│   └── daily_collection.py         ← Main collection script
├── reports/
│   └── collection_2026-03-26.md    ← Daily summaries (growing)
├── .brain_local/                   ← gitignored working memory
│   └── collections/
│       └── raw_collection_2026-03-26.json
└── .gitignore                      ← Includes .brain_local/
```

---

## How to test

```bash
# Dry run (no modifications)
python3 scripts/daily_collection.py --dry-run --verbose

# Real run
python3 scripts/daily_collection.py --verbose

# Check output
cat reports/collection_*.md
cat .brain_local/collections/raw_collection_*.json
```

---

## API keys & credentials

For repos that need auth (private repos) or URLs that need API keys:

Store in environment or `.env.local` (never committed):
- `GITHUB_TOKEN` — for private repos
- `ANTHROPIC_API_KEY` — for URL summarization (phase 2)

---

## Design principles

1. **Collection before classification** — Get all data first, organize later
2. **Temp cloning, no persistence** — Don't accumulate local repo copies
3. **Summaries committed, raw data local** — Team sees what was collected
4. **Incremental, not re-fetch** — Use timestamps to avoid redundant work
5. **Structured logging, graceful errors** — Know what failed and why
6. **Scheduled automation ready** — Built to run unattended daily

---

## Next major milestone

✅ Data flows in daily
🔲 Data gets classified and indexed
🔲 Synthesis engine finds connections
🔲 Decisions route to output destinations
