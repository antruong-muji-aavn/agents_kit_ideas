---
name: plan-feature
description: Planning a new klara-theme UI feature. Gathers context, produces 6 JSON plan artifacts, and validates the plan.
---

# Plan Feature

## Context

Load `web-ui-lib` skill — triggers `docs/index.json` KB discovery. For this stage load: CONV-0001 (structure), CONV-0003 (props), CONV-0006 (tokens), FEAT-0001 (component catalog).

## Inputs (required)

- `libs/klara-theme/.ai-agents/ui/<feature>/requirements.md`
- `libs/klara-theme/.ai-agents/ui/<feature>/figma_refs.md`
- `libs/klara-theme/.ai-agents/ui/<feature>/repo_context.md`
- `libs/klara-theme/.ai-agents/ui/<feature>/FigmaExtract_UI.json`

## Steps

### 1. Gather Context

- Read all input files for the feature
- Delegate RAG search to `epost-mcp-manager`: query = `"<component> props and variants"`, scope = web, type = component/utility
- Check existing components in `libs/klara-theme/src/lib/components/` for reuse opportunities

### 2. Produce Plan Artifacts

Write 6 JSON files to `libs/klara-theme/.ai-agents/ui/<feature>/`:

| File | Purpose |
|------|---------|
| `component-inventory.json` | All components needed, existing vs new |
| `variants-mapping.json` | Figma variants to React props mapping |
| `tokens-mapping.json` | Design tokens for spacing, color, typography |
| `integration-guidance.json` | How feature integrates with existing code |
| `implementation-guidance.json` | Technical implementation details |
| `implementation-order.json` | Ordered list of implementation steps |

### 3. Validate Plan

- Reuse-first: do not create new components if a close equivalent exists
- Tokens: no hardcoded values unless explicitly approved
- Storybook: plan Default/Sizes/Variants/States story reachability
- Verify all referenced tokens exist in the design token system

## Success Criteria

- All 6 plan artifacts written
- No unnecessary new components (reuse existing)
- All design tokens mapped (no hardcoded values)
- Storybook coverage planned for all variants
- Implementation order reflects dependency chain

## Next Step

Run `implement-component` aspect with the plan artifacts.
