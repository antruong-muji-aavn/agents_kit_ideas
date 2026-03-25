---
name: implement-component
description: Implementing a klara-theme component or layout from plan artifacts. Covers pre-check, code implementation, Storybook stories, exports, and verification.
---

# Implement Component

## Context

Load `web-ui-lib` skill — triggers `docs/index.json` KB discovery. For this stage load: CONV-0001 (structure), CONV-0003 (props), CONV-0005 (translations), CONV-0006 (tokens).

## Inputs (required)

- `libs/klara-theme/.ai-agents/ui/<feature>/component-inventory.json`
- `libs/klara-theme/.ai-agents/ui/<feature>/variants-mapping.json`
- `libs/klara-theme/.ai-agents/ui/<feature>/tokens-mapping.json`
- `libs/klara-theme/.ai-agents/ui/<feature>/implementation-guidance.json`
- `libs/klara-theme/.ai-agents/ui/<feature>/FigmaExtract_UI.json`

## Steps

### 1. Pre-Implementation Check

- Read all plan artifacts
- Delegate RAG search to `epost-mcp-manager`: query = `"<target> existing implementation"`, scope = web, type = component/layout
- Review existing component patterns in codebase

### 2. Implement Component

Write code to:
- `libs/klara-theme/src/lib/components/<component-name>/**` (for components)
- `libs/klara-theme/src/lib/layouts/<layout-name>/**` (for layouts)

Follow conventions from `libs/klara-theme/CLAUDE.md`:
- Component development patterns (forwardRef, displayName, etc.)
- Naming and file structure conventions
- Design token usage (no hardcoded values)
- TypeScript strict mode

### 3. Write Storybook Stories

Create `*.stories.tsx` covering:
- **Default** — base render
- **Sizes** — all size variants
- **Variants** — visual variants (primary, secondary, etc.)
- **States** — interactive states (disabled, loading, error, etc.)

Follow Storybook conventions from `libs/klara-theme/CLAUDE.md`.

### 4. Export Component

- Add exports to the component's `index.ts`
- Update `libs/klara-theme/src/index.ts` if needed

### 5. Verify

- Run `nx lint klara-theme`
- Run `nx test klara-theme`
- Run `npm run storybook-theme-build` to verify stories compile
- Fix any issues before completion

## Success Criteria

- Component matches plan artifacts
- All variants implemented per variants-mapping
- Design tokens used (no hardcoded values)
- Stories cover Default/Sizes/Variants/States
- Lint and tests pass
- Storybook builds successfully

## Next Step

Run `audit-ui` aspect to compare implementation against plan and design.
