---
name: audit-ui
description: Auditing a klara-theme component implementation against its plan and Figma design. Produces structured audit-report.json with findings.
---

# Audit UI

## Context

Load `web-ui-lib` skill — triggers `docs/index.json` KB discovery. For this pipeline stage load: CONV-0001 (structure), CONV-0003 (props), CONV-0006 (tokens).

## Inputs (required)

- `libs/klara-theme/.ai-agents/ui/<feature>/component-inventory.json`
- `libs/klara-theme/.ai-agents/ui/<feature>/variants-mapping.json`
- `libs/klara-theme/.ai-agents/ui/<feature>/tokens-mapping.json`
- `libs/klara-theme/.ai-agents/ui/<feature>/FigmaExtract_UI.json`
- Code under `libs/klara-theme/src/lib/**`

## Steps

### 1. Collect Evidence

- Read plan artifacts and implemented code
- Optionally use Figma MCP for visual comparison: `figma/get_screenshot(nodeId)`
- Delegate RAG search to `epost-mcp-manager`: query = `"<component> variants"`, scope = web, type = component/utility

### 2. Compare Plan vs Implementation

Check each area:
- **Visual**: Does the component match the Figma design?
- **Tokens**: Are all design tokens correctly applied (no hardcoded values)?
- **Patterns**: Does code follow klara-theme conventions?
- **Variants**: Are all planned variants implemented?
- **Responsive**: Does the component handle responsive behavior?

### 3. Write Audit Report

Output to `libs/klara-theme/.ai-agents/ui/<feature>/audit-report.json`:

```json
{
  "findings": [
    {
      "id": "UI-001",
      "severity": "error|warning|info",
      "category": "visual|token|pattern|variant|responsive",
      "location": "path:line (or story id)",
      "expected": "short description",
      "actual": "short description",
      "reference": "plan section + figma nodeId"
    }
  ]
}
```

### 4. Summarize

- Total findings by severity
- Recommendation: pass, fix-and-reaudit, or redesign

## Success Criteria

- All plan artifacts compared against implementation
- Findings documented with clear severity and category
- Each finding includes location, expected vs actual, and reference
- Actionable report ready for `fix-findings` aspect

## Next Step

If findings exist, run `fix-findings` aspect with the audit report.
