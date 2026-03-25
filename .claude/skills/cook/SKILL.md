---
name: cook
description: (ePost) Use when user says "implement", "build", "add a feature", "cook", "make this work", or "continue the plan" — dispatches platform-aware feature implementation for web, iOS, Android, or backend
user-invocable: true
context: fork
agent: epost-fullstack-developer
metadata:
  argument-hint: "[feature description or plan file]"
---

# Cook — Unified Implementation Command

Implement features with automatic platform detection.

## Step 0 — Active Plan Resolution (when args are empty)

**If `$ARGUMENTS` is empty**, resolve the active plan before asking the user for a task:

1. Run: `node .claude/scripts/get-active-plan.cjs`
2. **If result ≠ `none`**: read the plan's `status.md` FIRST (if it exists), then `plan.md`. Identify the first phase with `status: pending` and implement it — no need to ask the user what to do.
3. **If result = `none`**: scan `plans/*/plan.md` for the most recently created plan with `status: pending` (plans just created by `/plan` that haven't been activated yet). Sort by directory name descending; take the first match.
4. **If still nothing**: ask the user for a task description.

When a plan is found via step 3 (frontmatter scan), run `node .claude/scripts/set-active-plan.cjs <plan-dir>` to activate it before proceeding.

## Status Tracking

### On Resume
Read `{plan_dir}/status.md` FIRST to recover full context before reading plan.md.
status.md = current state (what happened). plan.md = spec (what to build).

### After Completing a Phase
If a workaround was needed during this phase (a tool, pattern, or convention that wasn't covered by loaded skills), note it in the journal entry "What almost went wrong" section with the skill name that should have caught it. This feeds the skill evolution pipeline.

Update `{plan_dir}/status.md`:
1. Progress table: change phase status to `Done`
2. Remove phase from **Not Yet Started** when it starts (change to `In Progress` in Progress table)
3. Add any significant design decisions to **Key Decisions**:
   `| {today} | {what was decided} | {why} |`
4. If implementation revealed architecture: update **Architecture Reference**

### After Discovering a Bug During Implementation
Add to `{plan_dir}/status.md` **Known Bugs**:
```
- {what is broken} — {steps to reproduce or context}
```
When fixed, move to **Recently Fixed**: `- {what was broken} — {how it was fixed}`

## Step 1 — Flag Override

If `$ARGUMENTS` starts with `--fast`: skip auto-detection, load `references/fast-mode.md` and execute directly. Remaining args are the task description.
If `$ARGUMENTS` starts with `--parallel`: skip auto-detection, load `references/parallel-mode.md` and execute directly. Remaining args are the task description.
Otherwise: continue to Platform Detection.

## Aspect Files

| File | Purpose |
|------|---------|
| `references/fast-mode.md` | Direct implementation — skip plan question, implement immediately |
| `references/parallel-mode.md` | Parallel implementation for multi-module features |

## Platform Detection

Detect platform per `skill-discovery` protocol.

## Complexity → Variant

- Single file or clear task → fast (skip plan question)
- Multi-file, one module → fast (batch checkpoints active for >3 file changes)
- Multi-module or unknowns → parallel
- Has existing plan in ./plans/ → follow plan
- Plan with 3+ independent tasks → consider subagent-driven mode (see `subagent-driven-development` skill)

## Execution

Route to the detected platform agent with feature description and platform context.

<feature>$ARGUMENTS</feature>

**IMPORTANT:** Analyze the skills catalog and activate the skills needed for the detected platform.
