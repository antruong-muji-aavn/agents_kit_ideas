# AI Ecosystem Brain — Working Model

## Purpose

Central reference for how `agents_kit_ideas` operates as the intelligence brain
that collects, synthesizes, and routes ideas into a growing AI ecosystem for the org.

## Table of Contents

- [The Big Picture](#the-big-picture) → Lines 20–40
- [Project Map](#project-map) → Lines 41–70
- [Source Layer](#source-layer) → Lines 71–110
- [Brain Layer](#brain-layer) → Lines 111–170
- [Output Layer](#output-layer) → Lines 171–200
- [Dev Feedback Loop](#dev-feedback-loop) → Lines 201–250
- [Synthesis Engine](#synthesis-engine) → Lines 251–300

---

## The Big Picture

```
sources → brain → synthesis → outputs
```

The brain (`agents_kit_ideas`) is not a filing system. It is a **synthesis engine**.
Ideas flow in from multiple sources, get classified and indexed, then the brain looks
for connections — ideas that reinforce, complete, or combine with each other before
routing to an output destination.

Key principle: **real dev friction outweighs external cool**. A pattern three devs
hit repeatedly ranks higher in synthesis than an interesting GitHub find.

---

## Project Map

| Project | Role | Path |
|---------|------|------|
| `agents_kit_ideas` | Central brain — collect, synthesize, route | `/Users/than/Projects/agents_kit_ideas` |
| `epost_agent_kit` | Primary kit — org AI ecosystem for devs | `/Users/than/Projects/epost_agent_kit` |
| `agent-kernel` | Internal dev contributions stream | `/Users/than/Projects/agent-kernel` |
| `agents_kit_references` | Curated external projects (clone or link) | `/Users/than/Projects/agents_kit_references` |
| feedback repo | Dev usage signals and friction notes | not yet set up → see Dev Feedback Loop |

### Relationship between agent-kernel and agents_kit_references

`agent-kernel` = internal trust level. Dev contributions from the org. Fast loop, daily indexing.

`agents_kit_references` = external trust level. Cool projects found online, cloned or linked.
Slower loop, curated manually, higher bar to adopt.

`agent-kernel` could structurally live inside `references/` but is kept separate because
it gets a faster automated pipeline and different weighting in synthesis.

---

## Source Layer

Four input streams feed the brain:

### 1 · agent-kernel (internal, daily)
- Dev contributions from the org
- Monitored by scheduled task → see `AGENT_KERNEL_DAILY_INTEL.md`
- Trust: high — same team, same context

### 2 · agents_kit_references (external, periodic)
- Manually curated external projects
- Clone locally or store as link + notes
- Trust: medium — vetted but different context
- Review cadence: weekly or when new project added

### 3 · Internet research (external, directional)
- Open research triggered by a direction or topic list
- Not random browsing — brain defines research directions
- Trust: low until classified and cross-referenced
- Review cadence: on-demand, direction-driven

### 4 · Dev feedback signals (internal, continuous)
- Passive: kit auto-journals usage as devs work
- Intentional: devs write notes when they hit friction
- Committed to feedback repo at their own pace
- Trust: highest — real usage, real pain
- See: Dev Feedback Loop section

---

## Brain Layer

### Directory structure

```
/Users/than/Projects/agents_kit_ideas/
├── .brain/
│   ├── .last_run                     ← timestamp of last cycle
│   ├── index/                        ← master idea index (all sources)
│   │   └── index_<YYYY-MM-DD>.md
│   ├── classifications/              ← per-source classification logs
│   │   └── classification_<source>_<YYYY-MM-DD>.md
│   ├── synthesis/                    ← cross-idea connection findings
│   │   ├── compounds/                ← ideas merged into something stronger
│   │   └── waiting/                  ← incomplete ideas pending more signal
│   ├── summaries/                    ← daily brain output
│   │   └── summary_<YYYY-MM-DD>.md
│   └── decisions/                    ← routed action log
│       └── decisions_<YYYY-MM-DD>.md
├── topics/                           ← active research directions
│   └── TOPICS.md
├── todo/                             ← phased todo list
│   └── TODO.md
└── .gitignore                        ← .brain/ is gitignored
```

### Gitignore rule

`.brain/` is never committed. It is the brain's working memory — local only.
Actioned outputs (things that get COPY/BUILD/ENRICH decisions) go to their
destination project and are committed there.

```gitignore
# Brain working memory — local only
.brain/
```

### Classification categories

| Category | What it covers | Examples |
|----------|---------------|---------|
| `skill` | Reusable playbooks, capabilities | `SKILL.md`, tool wrappers |
| `knowledge` | RAG data, source-of-truth files | `.json`, knowledge bases |
| `structure` | Directory layout, scaffolding | New folders, index files |
| `layer` | Orchestration, agent roles | Agent system prompts, routing logic |
| `setup` | Config, environment, bootstrap | `platforms.yaml`, `.env`, install scripts |
| `workflow` | SOPs, pipelines, automation | `.yaml` workflows, shell scripts |
| `instruction` | Rules, constraints, guidelines | `.mdc`, `CLAUDE.md`, rule files |
| `ui_component` | Frontend/design-to-code pieces | React components, design tokens |
| `mcp_pattern` | MCP server patterns, connector designs | server stubs, tool schemas |
| `dev_tool` | CLI tools, scripts for dev workflow | shell scripts, helper CLIs |
| `feedback` | Dev usage signals and friction notes | journal entries, friction notes |

If no category fits → propose a new one with `🆕`, define it, track it.
At 3 confirmed uses across different days it graduates to the table above.

---

## Brain Cycle (per scheduled run)

```
1. collect    → pull new content from all active sources
2. classify   → assign categories to each new item
3. index      → add to master index with tags, origin, date
4. synthesize → find connections across indexed ideas
5. summarize  → write daily summary with decisions and routing
```

### Synthesis questions (run against index each cycle)

- Do any two ideas share a category AND target the same problem space?
- Does a new idea fill a gap that a waiting idea identified?
- Does combining A + B produce something none of the current outputs already have?
- Has the same pattern appeared from 2+ different sources independently?

### Synthesis outcomes

| Outcome | Meaning | Next step |
|---------|---------|-----------|
| Idea ready | Strong enough alone | Route to output destination |
| Compound idea | A + B stronger together | Merge entry, route combined |
| Needs more | Incomplete signal | Move to `waiting/`, revisit next cycle |

---

## Output Layer

Ideas route to one of five destinations:

| Destination | What it is | Decision type |
|-------------|-----------|---------------|
| `epost_agent_kit` | Primary kit — skills, agents, workflows | COPY / ENRICH |
| MCP server | Custom integration or connector | BUILD |
| Dev tool | CLI, script, automation helper | BUILD |
| Automation project | Standalone AI automation | INSPIRE → new project |
| Org standard | Shared patterns, conventions, rules | STANDARDIZE |

### Decision values

| Value | Meaning |
|-------|---------|
| `COPY` | Bring directly into destination as-is |
| `ENRICH` | Use to improve an existing file in destination |
| `REFERENCE` | Keep external, link and note — don't bring in |
| `BUILD` | Use as foundation to build something new |
| `INSPIRE` | Seed for a net-new project or tool |
| `STANDARDIZE` | Elevate to org-wide pattern or rule |
| `SKIP` | Not relevant |
| `DECIDE` | Needs human review before action |

---

## Dev Feedback Loop

### How it works

The kit itself is the sensor. Two signal modes:

**Passive** — kit writes automatically as dev works:
- Skills invoked and frequency
- Output style patterns
- Active projects
- Workarounds (commands run that aren't in any skill)

**Intentional** — dev writes a note when they notice something:
- "I wish the kit could..."
- "This pattern kept appearing"
- "This was painful / missing"
- "I had to work around X by doing Y"

### Journal location (per dev, local)

```
~/.kit-journal/
├── .last_active
├── passive/
│   └── <YYYY-MM-DD>.jsonl    ← auto-written by kit
└── notes/
    └── <YYYY-MM-DD>.md       ← dev writes manually
```

Gitignored locally. Dev commits to feedback repo when ready.

### Committing to feedback repo

```
kit feedback commit          # bundles today's journal + notes, opens commit
kit feedback push            # commit + push to feedback repo
```

Dev controls when to share. No automatic push.

### Feedback repo structure (to set up)

```
feedback-repo/
└── <username>/
    └── <YYYY-WW>/            ← year + week number
        ├── passive.jsonl
        └── notes.md
```

### Brain reads feedback repo

- On each brain cycle, pull latest from feedback repo
- Classify entries as category `feedback`
- Weight higher than external sources in synthesis
- A friction note from 2+ devs → immediate candidate for COPY or BUILD

---

## Synthesis Engine

### Idea entry format (in index)

```markdown
## idea:<id>

- date: <YYYY-MM-DD>
- source: agent-kernel | references | internet | feedback
- origin: <file path or URL>
- category: <category>
- tags: [tag1, tag2]
- summary: one sentence
- status: new | waiting | ready | compounded | actioned
- compounds_with: <idea-id> (if merged)
- destination: <output destination> (if routed)
```

### Compound idea entry format

```markdown
## compound:<id>

- date: <YYYY-MM-DD>
- ideas: [idea:001, idea:047]
- synthesis_reason: why A + B together is stronger
- category: <shared or new category>
- destination: <output destination>
- status: ready | waiting | actioned
```

### Waiting pool rules

- Max wait: 14 days before a waiting idea is reviewed for drop
- If no new signal after 14 days → mark `stale`, move out of active synthesis
- Stale ideas kept in `waiting/stale/` — not deleted, just deprioritized
