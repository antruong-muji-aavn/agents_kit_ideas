---
name: fix-findings
description: Resolving audit findings from the audit-ui process. Applies minimal fixes, maintains stable API, and produces fix output.
---

# Fix Findings

## Context

Read `libs/klara-theme/CLAUDE.md` for component patterns and conventions.

## Inputs (required)

- `libs/klara-theme/.ai-agents/ui/<feature>/audit-report.json`

## Steps

### 1. Analyze Findings

- Read the audit report
- Group findings by root cause:
  - **Token drift** — wrong or missing design tokens
  - **Layout rules** — spacing, alignment, responsive issues
  - **Variant logic** — missing or incorrect variant handling
  - **Pattern violations** — not following klara-theme conventions

### 2. Apply Fixes

- Patch smallest surface area first (minimal diffs)
- Maintain stable component API (no breaking changes)
- Fix in priority order: errors -> warnings -> info

### 3. Update Related Files

- Update stories/tests if fix changes behavior
- Update plan artifacts if design intent was misunderstood

### 4. Produce Fix Output

Preferred: `libs/klara-theme/.ai-agents/ui/<feature>/PATCH.diff`
Alternate: `libs/klara-theme/.ai-agents/ui/<feature>/fix-notes.json`

### 5. Verify

- Run `nx lint klara-theme`
- Run `nx test klara-theme`
- Run `npm run storybook-theme-build`

## Success Criteria

- All error-severity findings resolved
- Warning-severity findings resolved or documented
- No breaking API changes introduced
- Lint, tests, and Storybook build pass
- Fix output documented

## Next Step

Request re-audit via `audit-ui` aspect to confirm fixes.
