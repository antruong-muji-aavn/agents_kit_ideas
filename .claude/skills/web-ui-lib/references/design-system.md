---
name: knowledge/klara-theme/design-system
description: "klara-theme token architecture — use RAG for current values"
source: dynamic
---

# klara-theme Design System

## 3-Layer Token System (stable architecture)

1. **Raw tokens** — Absolute values (colors, sizes)
2. **Semantic tokens** — Contextual meaning (--color-primary, --spacing-md)
3. **Component tokens** — Component-specific (--button-padding, --card-radius)

## How to Find Current Token Values

> Token values change. Always query RAG for current values.

### Primary: MCP query tool
```
query({ query: "color tokens", filters: { topic: "design-system", file_type: "scss" } })
query({ query: "spacing scale", filters: { topic: "design-system" } })
query({ query: "typography tokens", filters: { topic: "design-system" } })
```

### Fallback: Grep
```
Grep: --color- or --space- or --text- in libs/klara-theme/src/lib/styles/
```
