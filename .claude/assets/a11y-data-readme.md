# .epost-data/a11y/

Accessibility findings database and report archive. Auto-managed by a11y commands.

## Structure

```
fixes/
├── findings/   ← /audit-a11y JSON reports (audit-YYMMDD-HHMM.json)
├── patches/    ← /fix-a11y unified diffs (finding-{id}-YYMMDD.diff)
└── reviews/    ← /review-a11y JSON reports (review-YYMMDD-HHMM.json)
analysis.md     ← trend summary across audits
known-findings.json ← findings DB (schema: known-findings-schema.json)
```

## Workflow

1. `/audit-a11y` or `/review-a11y` → detects violations → persists to `known-findings.json` + saves report to `fixes/findings/` or `fixes/reviews/`
2. `/fix-a11y` → reads from `known-findings.json` → applies fix → saves diff to `fixes/patches/` → marks `fix_applied: true`
3. `/audit-close-a11y <id>` → marks finding as `resolved: true` after human verification

## Notes

- This directory is **gitignored** — runtime state, not source-tracked
- Schema lives at `packages/a11y/assets/known-findings-schema.json`
- First run of any a11y command auto-creates missing directories
