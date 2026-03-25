# Inspirations — Brain Source Manifest

Central registry of all sources the brain monitors daily.

## How it works

Each `.md` file in this folder lists sources of one type:

- **agent-kernel.md** — Internal org contributions (repo)
- **agents-kit-references.md** — Curated external projects (repos)
- **external-links.md** — Specific URLs to monitor
- **internet-research.md** — Research directions (keywords, queries)
- **custom-prompts.md** — Ad-hoc research tasks for Claude

The daily collection script reads this folder, processes each source, and saves raw results to the cloud brain.

## Source format

### Repo (git)
```
## Source name

`repo:<URL or local path>`

- Trust level: high|medium|low
- Notes: <context>
```

### Folder (filesystem)
```
## Source name

`folder:<local path>`

- Monitors: file changes only
- Notes: <context>
```

### URL (http)
```
## Source name

`url:<HTTP(S) URL>`

- Trust level: medium|low
- Frequency: daily (summary) or on-demand
- Notes: <context>
```

### Prompt (research direction)
```
## Research topic

`prompt:<natural language instructions>`

- Trust level: N/A
- When: on-demand or scheduled
- Notes: <context>
```

## Example entries

See individual `.md` files for populated examples.

## Adding a new source

1. Decide which file it belongs in (or create a new one)
2. Add entry with name, source line, and metadata
3. Daily script will pick it up next run
4. Results appear in cloud brain `collections/` folder

## Cloud storage

Raw collection output saves to cloud brain (TBD location).
Summaries commit locally to `reports/` folder.
