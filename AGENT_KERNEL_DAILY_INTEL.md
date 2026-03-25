# Agent Kernel — Daily Intel

## Paths

| | Path |
|-|------|
| Source | `/Users/than/Projects/agent-kernel` |
| Brain | `/Users/than/Projects/agents_kit_ideas/.brain/` |
| State | `/Users/than/Projects/agents_kit_ideas/.brain/.last_run` |

---

## Run

### 1 — Ensure brain scaffold exists

Check `/Users/than/Projects/agents_kit_ideas/.brain/`. If missing, create:

```
.brain/
├── .last_run
├── index/
├── classifications/
├── synthesis/
│   ├── compounds/
│   └── waiting/
├── summaries/
└── decisions/
```

Check `/Users/than/Projects/agents_kit_ideas/.gitignore` contains `.brain/`.
If missing, add it before creating anything else.

---

### 2 — Get changes

```bash
cd /Users/than/Projects/agent-kernel
git pull
git log --since="$(cat /Users/than/Projects/agents_kit_ideas/.brain/.last_run)" --name-status --format=""
```

If `.last_run` missing → use 24h ago as baseline.

Save changed file list to:
`.brain/index/changed_agent-kernel_<YYYY-MM-DD>.txt`

---

### 3 — Classify each changed file

Assign one or more categories per file.

| Category | Covers |
|----------|--------|
| `skill` | Reusable playbooks, `SKILL.md`, tool wrappers |
| `knowledge` | RAG data, `.json` knowledge bases |
| `structure` | Directory layout, scaffolding, index files |
| `layer` | Agent system prompts, orchestration, routing |
| `setup` | Config, `.env`, install scripts, `platforms.yaml` |
| `workflow` | `.yaml` pipelines, SOPs, shell automation |
| `instruction` | `.mdc`, `CLAUDE.md`, rule files, constraints |
| `ui_component` | React components, design tokens |
| `mcp_pattern` | MCP server stubs, connector patterns, tool schemas |
| `dev_tool` | CLI scripts, developer workflow helpers |

If no category fits, propose one:

```
🆕 Proposed: `eval`
Definition: Test harnesses for measuring agent output quality
File: `evals/score_retrieval.py`
Count: 1/3
```

Proposed categories graduate to the table above at 3 confirmed uses across different days.

**Write to:** `.brain/classifications/classification_agent-kernel_<YYYY-MM-DD>.md`

```markdown
# Classification — agent-kernel — <YYYY-MM-DD>

## skill
- `skills/new-skill.md` — one line reason

## mcp_pattern
- `connectors/slack-bridge.ts` — one line reason

## 🆕 eval (1/3)
Definition: Test harnesses for measuring agent output quality
- `evals/bench.py` — one line reason
```

---

### 4 — Index new ideas

One entry per classified file.

**Write to:** `.brain/index/index_<YYYY-MM-DD>.md`

```markdown
## idea:ak-<YYYYMMDD>-<001>

- date: <YYYY-MM-DD>
- source: agent-kernel
- origin: `<file path>`
- category: <category>
- tags: [tag1, tag2]
- summary: one sentence
- status: new
- destination: TBD
```

---

### 5 — Synthesis check

Scan `.brain/synthesis/waiting/` for any waiting idea whose gap is addressed by today's new ideas.

- Match found → update waiting idea `status: ready`, note the connection
- No match → write "no compounds today"

---

### 6 — Write summary

**Write to:** `.brain/summaries/summary_<YYYY-MM-DD>.md`

```markdown
# Agent Kernel Intel — <YYYY-MM-DD>

## TL;DR
2–3 sentences on what changed and why it matters.

## Strong signals

| Idea ID | File | Category | Why | Action |
|---------|------|----------|-----|--------|
| ak-...-001 | `skills/x.md` | skill | fills gap in kit | COPY → epost_agent_kit |
| ak-...-002 | `connectors/y.ts` | mcp_pattern | MCP server seed | BUILD → new project |

## Skip

| File | Reason |
|------|--------|
| `setup/docker.yml` | different infra |

## Synthesis

<match found or "no compounds today">

## Decide (needs human review)

- [ ] `file` — reason

## Proposed categories

<list any 🆕 with count, or "none">
```

---

### 7 — Update state

```bash
date +%Y-%m-%d > /Users/than/Projects/agents_kit_ideas/.brain/.last_run
```

Do not stage or commit anything under `.brain/`.
