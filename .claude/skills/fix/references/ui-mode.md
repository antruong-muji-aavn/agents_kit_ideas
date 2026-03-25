---
name: fix-ui-mode
description: "(ePost) Fix UI component findings from known-findings DB"
user-invocable: false
metadata:
  argument-hint: "<ComponentName> [--finding-id <id>] [--top <n>] [--no-verify]"
---

# Fix UI Mode

Invoked when: `fix --ui <ComponentName> [--finding-id <id>] [--top <n>] [--no-verify]`

Executes inline in main context — the main context dispatches epost-muji via Agent tool.

## Flags

| Flag | Behavior |
|------|----------|
| `--finding-id <id>` | Apply only the specified finding |
| `--top <n>` | Apply top N unresolved findings by severity |
| `--no-verify` | Skip the automatic re-audit after applying fixes (step 7.5) |

## Steps

1. Parse `$ARGUMENTS`:
   - If no component name provided: ask "Which component? (e.g. `EpostButton`)" and wait for reply
2. Load `reports/known-findings/ui-components.json`
   - If file not found: report "no UI findings DB — run `/audit --ui <ComponentName>` first" and stop
3. Select finding(s):
   - `--finding-id <id>`: load that specific finding
   - `--top <n>`: load top N unresolved by severity (critical → high → medium → low)
   - No flag: load all unresolved findings for named component
4. Delegate to epost-muji via Agent tool with:
   - Finding objects from DB
   - Component name + `file_pattern`
   - Mode: **plan** (produce fix plan + diff preview — do NOT write files yet)
   - Boundaries: plan ONLY the flagged rule violation — no opportunistic improvements
5. Present fix plan to user. For each finding, show:
   ```
   Finding #7 — PROPS-001 (high)
   File: src/lib/components/smart-letter-composer/smart-letter-composer.tsx
   Issue: Props interface not exported
   Fix: Export ISmartLetterComposerProps + add JSDoc on 3 undocumented props
   Diff preview:
     - type SmartLetterComposerProps = { ... }
     + export interface ISmartLetterComposerProps { ... }
   Confidence: high
   ```
6. **Ask for confirmation**: "Apply these N fix(es)? (yes / skip #id / cancel)"
   - `yes` → proceed to step 7
   - `skip #id` → exclude that finding, apply the rest
   - `cancel` → stop, nothing written
7. Dispatch epost-muji via Agent tool with confirmed findings:
   - Mode: **apply** (write changes to source files)
7.5. **Targeted Re-Audit** (unless `--no-verify` flag present):
   - Collect the list of files modified in step 7
   - Collect the rule IDs from the applied findings
   - Dispatch epost-muji via Agent tool with:
     - Mode: **verify** (check specific rules only — not full audit)
     - Files: only the files changed in step 7
     - Rules: only the rule IDs from the applied findings
   - Parse result: for each finding, set `verified: true` if the rule now passes, `verified: false` if still failing
   - Output re-audit result: "Re-audit: N/M findings resolved" or "Re-audit: N/M resolved, K still failing (RULE-001, …)"
8. Update `reports/known-findings/ui-components.json`:
   - Set `fix_applied: true`, `fix_applied_date: today` for each applied finding
   - If step 7.5 ran: set `verified: true/false`, `verified_date: today` for each finding
   - If `--no-verify`: leave `verified: false`, `verified_date: null` (no implicit verification)
9. Output: files changed, lines changed per finding
10. Conditional suggestion:
    - If all `verified: true` (or `--no-verify` used): "Run `/audit --close --ui <id>` to mark as fully resolved"
    - If some `verified: false`: "N finding(s) still failing. Re-fix with `/fix --ui {component} --finding-id <id>` or review manually"

## Boundaries

- Fix ONE rule violation per finding — no opportunistic improvements
- Re-audit (step 7.5) is scoped to changed files + specific rules only — not a full audit
- If fix requires structural change (STRUCT category) — report instead of fixing, suggest redesign

## Schema Reference

See `audit/references/ui-findings-schema.md` for field definitions and resolution state machine.
