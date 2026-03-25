# Prototype Conversion

Convert external prototypes, mockups, or codebases into production-ready code using design system components, semantic design tokens, and proper module architecture.

## Reference Files

| File | Purpose |
|------|---------|
| `references/analysis-checklist.md` | Prototype inventory and framework detection |
| `references/component-mapping.md` | Map common patterns to klara-theme equivalents |
| `references/token-mapping.md` | Map hardcoded values to klara-theme tokens |
| `references/style-migration.md` | Migrate style systems to klara-theme |
| `references/data-migration.md` | Replace mock data with real API integration |

## Conversion Workflow

1. **Analyze** — Inventory prototype: frameworks used, component count, state management, API calls
2. **Map Components** — Map to klara-theme equivalents (buttons, inputs, layouts, etc.)
3. **Map Tokens** — Replace hardcoded values with klara-theme tokens
4. **Migrate Styles** — Convert style system to klara-theme / Tailwind
5. **Integrate Data** — Replace mock data with real API integration
6. **Module Structure** — Place in correct module with `_components/` etc.

## Quick Reference

- Import: `@luz-next/klara-theme`
- Props: `styling` (not `variant`), `size`, `mode`, `radius`
- Tokens: `bg-base-background`, `p-200`, `gap-100`
- Data flow: Component -> Hook -> Action -> Service -> API

## Related Skills

- `web-modules` — Module integration patterns
- `web-ui-lib` — klara-theme component reference
- `domain-b2b` — Module knowledge
