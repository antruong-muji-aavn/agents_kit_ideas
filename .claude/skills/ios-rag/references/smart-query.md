# RAG Smart Query — HyDE + Multi-Query Retrieval (iOS)

## Purpose

Improve RAG recall on conceptual/abstract queries by generating hypothetical answers (HyDE) and variant queries before searching. Finds more relevant results than a single query alone.

## When to Use

| Query Type | Use Smart Query | Use Standard `query` |
|------------|:-:|:-:|
| Conceptual ("how does theming work?") | yes | partial |
| Cross-cutting ("accessibility in forms") | yes | partial |
| View relationships ("what uses EPButton?") | yes | partial |
| Vague/broad queries | yes | no |
| Specific file ("show me EPButton.swift") | no | yes |
| Known view name | no | yes |

## Process

### Step 1: Analyze Query

Identify before searching:
- **View/type names**: specific view or type (get canonical name from `expansions`)
- **Concepts**: dark mode, navigation, state management, accessibility
- **File types**: swift, storyboard, xib
- **Scope**: specific file vs. pattern vs. architecture

### Step 2: Generate Hypothetical Answer (HyDE)

Write a ~50-100 word hypothetical Swift code snippet or doc fragment that would appear in the ideal result. Embed and search this passage — it matches actual code better than a question does.

**Example:**

| User Query | HyDE Passage |
|-----------|-------------|
| "how does [concept] work?" | Write ~50-100 word hypothetical Swift code snippet describing implementation of the concept |
| "how to handle [pattern]?" | Write ~50-100 word hypothetical Swift code snippet showing the pattern in action |

### Step 3: Generate 1-2 Structural Variants

The RAG server auto-expands synonyms and recognizes component aliases (call `expansions` MCP tool to see current data). Do NOT generate synonym variants — generate structural variants only (different angle or scope).

**Note**: iOS server does word-by-word expansion only (no phrase matching). Multi-word synonym keys in iOS config (e.g., "dependency injection") are NOT matched as phrases yet.

| Strategy | Example (for "[concept]") |
|----------|-------------------------------|
| Type-specific (different scope) | "[Type] [RelatedType] protocol" |
| Architecture-level | "[concept] pattern protocol implementation" |

**Tips:**
- Use PascalCase for types — server handles alias expansion word-by-word
- Focus on structural differences, not synonym rephrasings
- Do NOT add synonym variants like "UIButton color token" or "btn appearance" — server handles these

### Step 4: Execute Queries

```
query(original_query, top_k=3)
query(hyde_passage_truncated_to_100_words, top_k=3)
query(structural_variant_1, top_k=3)
# optional: query(structural_variant_2, top_k=3)
```

Use `top_k=3` per query — yields up to 9-12 candidates before dedup.

### Step 5: Merge Results

1. **Deduplicate** by file path
2. **Boost** files appearing in 2+ query results
3. **Diversify** across views, models, protocols, extensions
4. **Present** best 5-8 unique results with source query context

## Server-Side Expansion

The iOS RAG server auto-expands queries before embedding:
- **Synonym expansion**: "button" -> "UIButton btn", "label" -> "UILabel", etc. (30+ groups)
- **Component recognition**: "PrimaryButton" -> detected, canonical name injected into query
- **Word-by-word matching**: each token expanded individually

**Limitation**: iOS server does NOT do multi-word phrase matching yet. "dependency injection" in config won't match as a phrase — only individual words "dependency" and "injection" expand separately.

Call `expansions` MCP tool (format: "full") for current synonym groups.
Call `expansions` MCP tool (format: "full") for canonical component names.

### Impact on Strategy

| Technique | Still Needed? | Why |
|-----------|:---:|-----|
| HyDE passage | YES | Server can't generate hypothetical Swift code |
| Structural variants (1-2) | YES | Different angle/scope — not synonyms |
| Synonym variants | NO | Server handles "UIButton"/"button"/"btn" |
| Component filter with canonical | YES | Use name from `expansions` MCP tool |

## Notes

- If original query returns 5+ high-score results, skip all variants
- For exact lookups, use `query` with component filter directly

## Related Documents

- `SKILL.md` — Main iOS RAG skill documentation
- `component-mappings.md` — How to get canonical names via `expansions` MCP tool
- `synonym-groups.md` — How to get synonym groups via `expansions` MCP tool
