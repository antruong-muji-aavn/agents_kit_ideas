# Evolve Mode — Skill Proposal Generation

Generate minimal, targeted skill improvement proposals from detected signals.

## Trigger

```bash
node .claude/scripts/extract-signals.cjs   # Step 1: gather signals
# Then: /plan --evolve                     # Step 2: generate proposals
```

Or as a standalone request: "generate skill proposals from signals"

## Step 1 — Load Signals

Read `docs/proposals/signals.json`. Filter for signals where:
- `status: "new"` (not yet proposed or dismissed)
- `confidence: "medium"` or `"high"` (skip `"low"` unless `--all` flag provided)

Group signals by `targetSkill` — generate **one proposal per skill**, not one per signal.
Include all signals for that skill as supporting evidence.

## Step 2 — For Each Skill Group

1. Read the target skill file at `targetFile` path
2. Find the gap: what rule, pattern, or check is missing based on the signal excerpts?
3. Identify the smallest change that addresses the gap (single section, max ~10 lines)
4. Write the proposal file

## Step 3 — Write Proposal File

File path: `docs/proposals/{skill-name}-{YYMMDD}.md`

```markdown
---
id: prop-{skill}-{YYMMDD}
targetSkill: {skill-name}
targetFile: packages/{package}/skills/{skill}/SKILL.md
signal: {comma-separated signal ids}
confidence: {highest confidence among grouped signals}
status: pending
created: {YYYY-MM-DD}
old_string: "exact text to replace in SKILL.md (must match verbatim)"
new_string: "replacement text"
---

# Proposal: {One-line title describing the change}

## Signal Sources

| Signal | Type | Excerpt |
|--------|------|---------|
| {id} | {type} | "{excerpt truncated to 80 chars}" |

## Current Skill Excerpt

```
{paste the current relevant section from SKILL.md}
```

## Proposed Change

```diff
- {old line(s)}
+ {new line(s)}
```

## Rationale

{1-2 sentences: why this change addresses the signal(s)}

## Risk

{What could break if this change is applied incorrectly}
```

## Step 4 — Update signals.json

After writing each proposal, update `docs/proposals/signals.json`:
- Set `status: "proposed"` for all signals included in the proposal

## Rules

- One proposal file per skill, even if multiple signals exist
- Proposals must be minimal — prefer adding a rule/example over rewriting a section
- `old_string` must exactly match current content in the target skill file (used for apply)
- Never write proposals to `packages/` directly — proposals are staging only
- If a signal points to `targetSkill: "unknown"`, skip it (cannot target a specific file)
- Skip proposals where the signal excerpt is too vague to suggest a concrete change

## Output Summary

After generating proposals, print:
```
Generated {N} proposals:
  {skill-name} → docs/proposals/{skill}-{date}.md  ({confidence})
  ...
Updated signals.json: {N} signals marked as 'proposed'
```
