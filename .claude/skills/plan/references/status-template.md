---
name: plan-status-template
description: Template and generation rules for living status.md files in plan directories
user-invocable: false
disable-model-invocation: true
---

# Plan Status Template

Living document combining progress, decisions, findings, architecture, and bugs in a single file.
The goal: full context recovery in under 30 seconds.

## Template

```markdown
# {Plan Title} — Status

> Quick-glance overview of what's done, what's next, and key decisions.
> **This is the first file to update when something changes.**

---

## Progress

| Phase | Description | Status |
|-------|-------------|--------|
| 1. {name} | {description} | Pending |

## Not Yet Started
- {list upcoming phases not in table}

## Deferred
- {scope cut or postponed — one line each with rationale}

---

## Known Bugs
None currently tracked.

### Recently Fixed
- {bug description} — {how it was fixed}

---

## Key Decisions

| Date | Decision | Rationale |
|------|----------|-----------|
| {YYYY-MM-DD} | {what was decided} | {why} |

## Open Decisions

| # | Question | Status |
|---|----------|--------|
| 1 | {unresolved question} | Open |

---

## Architecture Reference
{Living architecture notes — updated as implementation reveals structure}

---

*Last updated: {date}*
```

## Generation Rules

### When to Create
All plan modes (fast, deep, parallel) generate `status.md` alongside `plan.md`.
Create it as the final step before reporting plan completion.

### How to Pre-Populate

**Progress table** — extract from plan.md phases:
```
For each phase in plan.md:
  Add row: | {N}. {Phase Name} | {Phase Description} | Pending |
```

**Not Yet Started** — list all phases (they all start pending):
```
- Phase {N}: {Name}
```
(Remove a phase from this list once it moves to In Progress or Done)

**Key Decisions** — start empty (one placeholder row):
```
| {YYYY-MM-DD} | {first decision will appear here} | — |
```
Delete placeholder once first real decision is added.

**Architecture Reference** — start with:
```
TBD — will be populated during implementation.
```

**Known Bugs** — leave as "None currently tracked."

### For Deep Plans (add to Key Decisions)
Pre-populate Key Decisions with research-driven decisions from researcher reports:
```
| {created date} | Chose {approach} over {alternative} | {rationale from R1/R2} |
```

### For Parallel Plans (add to Architecture Reference)
Add file ownership matrix summary:
```
## Architecture Reference

**File Ownership Summary**
- Phase 1 owns: {files}
- Phase 2 owns: {files}
...

Execution batches:
- Batch 1 (parallel): {phases}
- Batch 2 (parallel, after Batch 1): {phases}
```

---

## Update Rules

### Cook — after completing a phase
1. Update Progress table: change phase status from `Pending` → `In Progress` (when starting) or `Done` (when completing)
2. Remove phase from **Not Yet Started** when it starts
3. Add any significant design decisions to **Key Decisions** table
4. Add any discovered architecture to **Architecture Reference**

### Fix — after fixing a bug
1. Add bug to **Known Bugs → Recently Fixed**: `- {what was broken} — {how it was fixed}`
2. If the fix required a design decision, also add to **Key Decisions**

### Debug — after diagnosing
1. Add findings to **Known Bugs** (if it's a bug) or **Key Decisions** (if it's an architectural discovery)
2. Note root cause, not just symptoms

### Resume Protocol
When cook reads an active plan, read `status.md` FIRST before plan.md.
status.md provides current state; plan.md provides the spec.

---

## Format Constraints

- status.md must be readable in <30 seconds
- Keep decisions concise: date + what + why (one line each)
- Sections can be empty, skipped, or extended as needed
- No required minimum length — an empty section is fine
- Do NOT duplicate content from plan.md (plan.md = spec, status.md = journey)
