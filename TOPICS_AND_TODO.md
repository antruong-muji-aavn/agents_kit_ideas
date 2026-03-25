# Topics & Todo — agents_kit_ideas

## Purpose

Active research directions and phased todo list for building the AI ecosystem brain.

## Table of Contents

- [Active Topics](#active-topics) → Lines 20–60
- [Phase 0 — Foundation](#phase-0--foundation-now) → Lines 61–100
- [Phase 1 — Brain Online](#phase-1--brain-online) → Lines 101–140
- [Phase 2 — Feedback Loop](#phase-2--feedback-loop) → Lines 141–180
- [Phase 3 — Synthesis](#phase-3--synthesis) → Lines 181–220
- [Phase 4 — Ecosystem Outputs](#phase-4--ecosystem-outputs) → Lines 221–260

---

## Active Topics

Research directions the brain should actively monitor and index.

| Topic | Why it matters | Sources to watch |
|-------|---------------|-----------------|
| MCP server patterns | Building custom connectors for dev workflow | agent-kernel, GitHub |
| Agent orchestration | How agents hand off, route, recover | agent-kernel, references |
| RAG for dev context | Making kits context-aware of the codebase | references, internet |
| Design-to-code pipelines | Figma → component automation | references, internet |
| Dev workflow automation | Reducing friction in daily dev tasks | feedback, agent-kernel |
| Skill composability | How skills call and extend each other | agent-kernel |
| Feedback loop design | How to capture implicit dev signals | internet, references |
| LLM eval patterns | Measuring agent output quality | internet, references |

---

## Phase 0 — Foundation (now)

Set up the project structure and core files before any automation runs.

### Todo

- [ ] Create `/Users/than/Projects/agents_kit_ideas/` folder
- [ ] Add `ECOSYSTEM_MODEL.md` (this working model) to project root
- [ ] Add `TOPICS.md` to `topics/` folder
- [ ] Add `TODO.md` to `todo/` folder
- [ ] Create `.gitignore` with `.brain/` entry
- [ ] Create `.brain/` directory scaffold (empty, gitignored)
- [ ] Confirm `/Users/than/Projects/agent-kernel` exists and is a git repo
- [ ] Confirm `/Users/than/Projects/agents_kit_references` exists (or create)
- [ ] Confirm `/Users/than/Projects/epost_agent_kit` exists and has `.gitignore`
- [ ] Add `AGENT_KERNEL_DAILY_INTEL.md` to `agents_kit_ideas/` as first schedule instruction

### Definition of done

Project folder exists. Core docs in place. `.brain/` gitignored. Paths confirmed.

---

## Phase 1 — Brain Online

Get the first scheduled task running: agent-kernel daily intel, writing to the brain.

### Todo

- [ ] Decide: where does agent-kernel live? (local clone path or remote URL)
- [ ] Set Claude schedule for `AGENT_KERNEL_DAILY_INTEL.md` — daily
- [ ] Run first manual cycle, verify `.brain/` scaffold is created correctly
- [ ] Verify `.brain/` is gitignored in `agents_kit_ideas`
- [ ] Confirm first `classification_agent-kernel_<date>.md` is written correctly
- [ ] Confirm first `summary_<date>.md` is written and readable
- [ ] Review first summary — do categories make sense?
- [ ] Adjust classification categories if needed
- [ ] Run for 5 days — check if any proposed categories appear → graduate if at 3 uses
- [ ] After 1 week: review `decisions/` log — any COPY or ENRICH actions to execute?

### Definition of done

Daily intel runs without errors for 7 days. Summaries are readable and actionable.

---

## Phase 2 — Feedback Loop

Build the dev journaling system so real usage signals feed the brain.

### Todo

#### Kit-side (epost_agent_kit)
- [ ] Design passive journal schema — what fields does a usage event have?
- [ ] Add journal writer to kit — triggers on skill invocation
- [ ] Write to `~/.kit-journal/passive/<YYYY-MM-DD>.jsonl`
- [ ] Add `kit journal "note text"` CLI command for intentional notes
- [ ] Write to `~/.kit-journal/notes/<YYYY-MM-DD>.md`
- [ ] Test passive capture with 2–3 devs for 1 week
- [ ] Review captured data — is it useful? too noisy? missing anything?

#### Feedback repo
- [ ] Create feedback repo (name TBD)
- [ ] Define folder structure: `<username>/<YYYY-WW>/`
- [ ] Add `kit feedback commit` command — bundles journal + notes
- [ ] Add `kit feedback push` command
- [ ] Add README to feedback repo explaining the contract
- [ ] Test full flow: use kit → auto-journal → `kit feedback commit` → push

#### Brain-side
- [ ] Add feedback repo as a source in brain cycle
- [ ] Add pull step: brain fetches latest feedback repo on each cycle
- [ ] Classify feedback entries under `feedback` category
- [ ] Implement weighting: feedback > external sources in synthesis scoring
- [ ] Test: does a friction note from 2 devs surface as high-priority in summary?

### Definition of done

At least 2 devs using kit, passive journal running, at least 1 feedback commit per week
reaching the brain and appearing in synthesis summary.

---

## Phase 3 — Synthesis

Move from classification to actual cross-idea connection finding.

### Todo

#### Index
- [ ] Design master index format (see `ECOSYSTEM_MODEL.md` — Idea entry format)
- [ ] Write index builder — reads all classification files, merges into master index
- [ ] Add dedup logic — same idea from 2 sources = one entry with multiple origins
- [ ] Test index builder on 2 weeks of accumulated classifications

#### Synthesis logic
- [ ] Implement synthesis question 1: shared category + same problem space
- [ ] Implement synthesis question 2: new idea fills gap in waiting idea
- [ ] Implement synthesis question 3: A + B → stronger compound
- [ ] Implement synthesis question 4: same pattern from 2+ independent sources
- [ ] Define compound idea format (see `ECOSYSTEM_MODEL.md`)
- [ ] Add waiting pool with 14-day rule and stale handling

#### Summary upgrade
- [ ] Update summary format to include Synthesis section
- [ ] Add compound ideas block to summary
- [ ] Add waiting pool status to summary (how many waiting, oldest date)
- [ ] Test: does the brain surface a compound idea that a human wouldn't have noticed?

### Definition of done

Brain produces at least one compound idea per week that connects two different sources.
Summary clearly distinguishes single ideas from compound ideas.

---

## Phase 4 — Ecosystem Outputs

Connect brain decisions to actual output destinations beyond epost_agent_kit.

### Todo

#### MCP server pipeline
- [ ] Define what a BUILD decision for an MCP server looks like in the summary
- [ ] Create `/Users/than/Projects/` folder structure for new MCP projects
- [ ] Define MCP server starter template (based on patterns from references)
- [ ] Test: brain identifies MCP pattern → human approves → scaffold created

#### Dev tool pipeline
- [ ] Define what a BUILD decision for a dev tool looks like
- [ ] Create dev tool starter template
- [ ] Test: brain identifies CLI pattern → scaffold created

#### Org standards pipeline
- [ ] Define what STANDARDIZE means in practice — where do org standards live?
- [ ] Create org standards repo or folder (TBD)
- [ ] Define format for a standard (rule file, convention doc, etc.)

#### Automation
- [ ] Can COPY decisions be executed automatically for low-risk files (docs, skills)?
- [ ] Define safe auto-copy rules — what needs human approval vs auto-OK
- [ ] Implement auto-copy for approved categories
- [ ] All auto-actions logged in `decisions/` with clear provenance

### Definition of done

Brain has produced at least one output in each destination category.
Decision log shows clear provenance for every action taken.

---

## Backlog (unphased)

Ideas and improvements not yet scheduled into a phase:

- Research direction management — how does the brain update `TOPICS.md` itself?
- agents_kit_references schedule — periodic review of external projects
- Internet research automation — define direction list, automate search + classify
- Multi-brain: could other projects run their own brain feeding into a shared synthesis?
- Metrics: how do we know the brain is producing value? What do we measure?
- Onboarding: how does a new dev learn the kit + feedback habit in < 10 minutes?
