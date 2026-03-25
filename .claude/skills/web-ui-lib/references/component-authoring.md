---
name: knowledge/klara-theme/component-authoring
description: "Key conventions for authoring a new klara-theme component"
---

# Building a klara-theme Component

> Full guide: `luz_next/libs/klara-theme/docs/component-guide.md`

## Quick Rules

### Structure
- Directory: `src/lib/components/{kebab-name}/`
- 7 required files: `tsx`, `styles.ts`, `stories.tsx`, `test.tsx`, `figma.json`, `mapping.json`, `index.ts`
- Compound sub-components: each gets its own `tsx` + `styles.ts`

### Props
- Interface: `I{PascalName}Props`
- Standard vocab: `styling` (not `variant`), `mode`, `size`, `radius`, `className`, `id`, `disabled`, `inverse`
- Variant consts: `SCREAMING_SNAKE as const`, types derived with `typeof CONST[keyof typeof CONST]`
- Internal props: prefix with `_`
- JSDoc every prop with `@default`

### Styles & Tokens
- All Tailwind in `*-styles.ts` — never inline in component body
- Variant maps: `Map<string, string>` assembled with `clsx()`
- Colors: semantic tokens only (`bg-base-*`, `text-signal-*`) — no raw colors
- Sizes: design scale tokens (`px-size-padding-sm`) — no raw px for theme dimensions
- State layer: shared `STATE_LAYER` utility — no custom hover/focus/disabled classes
- CSS vars: `--color-theme-base-*` only (never `--color-base-*`)

### Business Isolation
- Props accept primitives only — no domain/business types
- No API calls, no global state (Redux, Zustand)
- Brand theming via `BrandedWrapper` / `InverseWrapper` / `ThemedBox`
- No `_hooks/`, `_constants/`, `_types/` subdirs, no `CHANGELOG.md`

### Accessibility
- Root element: `theme-ui-label="component-name"`
- IDs: `useId()` + consumer override pattern
- Complex widgets: Radix UI primitives
- Focus + disabled: shared STATE_LAYER tokens

### Testing
- Test: render, variants, disabled, interactions, className passthrough
- Stories: `tags: ['autodocs']`, Base + Disabled + all styling variants
- Figma: non-empty `figma.json` + `mapping.json`

## When to Propose vs Build

- **New reusable component** → propose to MUJI (see `references/contributing.md`)
- **Business-specific variant** → build in app layer, not in klara-theme
- **Bug fix or new variant on existing component** → PR to klara-theme
