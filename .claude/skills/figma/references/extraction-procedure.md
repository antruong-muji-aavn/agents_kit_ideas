---
name: extraction-procedure
description: Step-by-step Figma data extraction procedure using Figma MCP tools. Covers component-level and feature-level extraction with anti-truncation handling.
---

# Figma Extraction Procedure

## Context

Read `libs/klara-theme/CLAUDE.md` (Figma Integration section) for MCP configuration and extraction patterns.

## Inputs

- **Component-level** (when called by document-component):
  - Component key: `<componentKey>`
  - Figma componentSet nodeId (if available)
- **Feature-level** (standalone extraction):
  - `libs/klara-theme/.ai-agents/ui/<feature>/figma_refs.md` (file URL + node IDs)

## Steps

### 1. Prepare

- Query Context7 for latest Figma MCP patterns:
  - `@context7 query-docs` — get current Figma MCP API usage
- Follow Figma integration patterns from `libs/klara-theme/CLAUDE.md`

### 2. Handle Missing nodeId

If Figma nodeId is not provided:
1. Ask user: "Please select the component frame in Figma"
2. Wait for user confirmation
3. Run `figma/get_metadata()` to get currently selected node
4. Extract nodeId from metadata response

### 3. Extract Data

For each relevant node (use smallest chunks to avoid truncation):
1. `figma/get_design_context(nodeId)` — component structure, layout, properties
2. `figma/get_variable_defs(nodeId)` — design tokens and variables
3. `figma/get_screenshot(nodeId)` — visual reference
4. If truncated: `figma/get_metadata(nodeId)` then refetch smaller child nodes

### 4. Produce Output

**Component-level** (return to calling workflow):
Return structured data object:
- `design_context`: component design data
- `variable_defs`: design tokens/variables
- `screenshot`: visual reference
- `nodeId`: the nodeId used

**Feature-level** (write files):
- `libs/klara-theme/.ai-agents/ui/<feature>/FigmaExtract_UI.json`
- `libs/klara-theme/.ai-agents/ui/<feature>/VariantMatrices.md`

## Success Criteria

- All Figma data extracted without truncation
- Design tokens mapped to klara-theme token system
- Visual reference captured
- Structured output ready for downstream skills
