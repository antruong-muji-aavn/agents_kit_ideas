# Brain Collection System — v2

## Overview

Single unified daily collection script that processes all idea sources.

```
inspirations/ → daily collection → cloud brain → local commit
```

---

## Inspirations Folder

Central manifest of all sources the brain monitors.

```
inspirations/
├── README.md                     ← format guide
├── agent-kernel.md               ← internal repo (daily)
├── agents-kit-references.md      ← external repos (daily)
├── external-links.md             ← URLs to monitor (daily)
├── internet-research.md          ← research directions (on-demand)
└── custom-prompts.md             ← raw research instructions (on-demand)
```

### Source types

| Type | Format | Example | Frequency |
|------|--------|---------|-----------|
| Repo | `repo:<GIT_URL>` or `repo:<LOCAL_PATH>` | `repo:https://github.com/org/agent-kernel` | Daily |
| Folder | `folder:<LOCAL_PATH>` | `folder:/Users/than/Projects/refs/` | Daily (file changes) |
| URL | `url:<HTTP_URL>` | `url:https://github.com/anthropics/anthropic-sdk` | Daily (summarize) |
| Prompt | `prompt:<text>` | `prompt:Find latest LLM eval frameworks` | On-demand |

### Entry format in each .md file

```markdown
## agent-kernel

`repo:https://github.com/org/agent-kernel`

- Trust level: high
- Last checked: 2026-03-25
- Notes: Internal contributions, fast cycle

## MCP patterns from SDK

`url:https://github.com/anthropics/anthropic-sdk/tree/main/examples`

- Trust level: medium
- Last checked: 2026-03-25
- Notes: MCP examples and patterns
```

---

## Daily Collection Script

Run once per day. Processes all enabled sources in `inspirations/`.

### Script flow

```
1. Read inspirations/ manifest
2. For each enabled source:
   a. If repo → git pull, get changes since last run
   b. If folder → scan for file modifications
   c. If URL → fetch and summarize
   d. If prompt → generate research task
3. Write raw collection to cloud brain:
   - `collections/collection_<YYYY-MM-DD>.md`
   - Include all source outputs
   - No classification yet (that's phase 2)
4. Local commit:
   - `reports/collection_<YYYY-MM-DD>.md` (summary only)
5. Update `.last_run` timestamp (cloud + local)
```

### Output format: `collection_<YYYY-MM-DD>.md`

```markdown
# Daily Collection — 2026-03-25

## agent-kernel

### Changed files (6 total)
- `skills/new_pattern.md` (added)
- `docs/orchestration.md` (modified)
- [...]

### Raw file list
[saved to cloud storage]

## agents-kit-references

### New repos added: 0
### Modified repos: 0

## external-links

### anthropic-sdk
- Last: 2026-03-24 @ v1.5.0
- Now: 2026-03-25 @ v1.5.1
- Summary: Patch release, no breaking changes

## internet-research

### "LLM eval frameworks"
- Sources found: 4
- Top: [...]
- Summary: [...]

## custom-prompts

### "Find latest feedback loop patterns"
- Task assigned: [assistant to run separately]
```

---

## Cloud Brain Storage

Structure (cloud):
```
cloud-brain/
├── .meta/
│   ├── .last_run
│   └── state.json           (cross-cycle intelligence)
├── collections/
│   ├── collection_2026-03-25.md
│   ├── collection_2026-03-24.md
│   └── [...]
├── raw/                     (unchanged files, large)
│   ├── agent-kernel_2026-03-25.json
│   ├── references_2026-03-25.json
│   └── [...]
└── [synthesis/ and summaries/ added in phase 2]
```

---

## Local Commits

What gets committed to `agents_kit_ideas` repo:

```
reports/
├── collection_2026-03-25.md   (summary only, human-readable)
├── collection_2026-03-24.md
└── [...]
```

This keeps the repo lightweight. Raw data lives in cloud.

---

## Next Steps

1. Choose cloud brain storage backend
2. Define inspirations/ entries
3. Implement daily collection script
4. Test on 3 sources (agent-kernel, 1 URL, 1 prompt)
