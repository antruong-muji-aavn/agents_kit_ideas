---
name: document-component
description: Creating or updating component-data documentation for a klara-theme component. Extracts Figma data, cross-references codebase, writes documentation files, and updates manifest.
---

# Document Component

## Context

Load `web-ui-lib` skill — triggers `docs/index.json` KB discovery. For this stage load: FEAT-0001 (component catalog), CONV-0001 (structure).

## Inputs (required)

- Component key: `<componentKey>`
- Figma componentSet nodeId (if available)
- Plan/design artifacts under `libs/klara-theme/.ai-agents/ui/<feature>/` (if available)

## Steps

### 1. Extract Figma Data

Use the `figma` skill extraction procedure:
- Provide `<componentKey>` and nodeId (if available)
- If nodeId missing, handle user interaction for selection
- Receive back: `design_context`, `variable_defs`, `screenshot`, `nodeId`

### 2. Cross-Reference Codebase

Delegate RAG search to `epost-mcp-manager`: query = `"<componentKey> implementation"`, scope = web

Optionally query Context7 for documentation patterns:
- `@context7 query-docs` — documentation best practices

### 3. Write Documentation Files

Create two files in `libs/klara-theme/src/lib/components/<componentKey>/`:

**`<componentKey>.figma.json`** — Component data
- Must validate against `libs/klara-theme/figma-data/schema/component-data.schema.json`

**`<componentKey>.mapping.json`** — Prop-to-design mapping
- Must validate against `libs/klara-theme/figma-data/schema/component-mapping.schema.json`

### 4. Update Manifest

Update `libs/klara-theme/figma-data/manifest.json`:
- Set component entry: `componentSetNodeId`, `path`, `status: "documented"`
- Set mapping entry: `path`, `status: "documented"`
- Increment `meta.documentedComponents` by 1
- Decrement `meta.pendingDocumentation` by 1

If Figma refs missing:
- Update `libs/klara-theme/figma-data/missing-components.json` (no placeholders)

### 5. Validate

- Verify JSON files against schemas
- Confirm manifest counts are correct

## Batch Mode

For documenting multiple components at once:

**Sequential** (error-tolerant):
```
for each pendingComponent:
  run document-component with componentKey
```

**Category batches** (parallel by category):
```
group components by category (form, ui, layout, etc.)
for each category:
  run document-component in parallel for all components in category
```

**Priority-based** (high-priority first):
```
run high-priority components sequentially
run medium and low-priority in parallel
```

### Batch Tracking

- Track: components processed / failed / total
- Retry failed components with adjusted params
- Continue on error (don't block batch)
- Per-component validation: JSON files exist, manifest updated, schemas valid

## Success Criteria

- `<componentKey>.figma.json` validates against schema
- `<componentKey>.mapping.json` validates against schema
- Manifest updated with correct status and counts
- Props mapped, nodeId captured
- No placeholder data
