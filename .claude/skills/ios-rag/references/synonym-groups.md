# iOS RAG — Synonym Groups

Live data served by RAG server. Call the `expansions` MCP tool to get current groups.

## How to Get Synonym Groups

Call `expansions` tool (format: "full") on the ios-rag MCP server.
Returns `synonyms` dict: group key → list of synonym terms.

## Impact on Query Strategy

| Technique | Use? | Reason |
|-----------|:----:|--------|
| HyDE passage | YES | Server can't generate hypothetical Swift code |
| Structural variants (1-2) | YES | Different angle, not synonyms |
| Synonym variants | NO | Server already handles |
| Component filter w/ canonical | YES | Use canonical from `expansions` MCP tool |

## Notes

- iOS server does word-by-word expansion only
- Multi-word synonym keys in config are NOT matched as phrases — only individual words expand
- If MCP tool unavailable, queries still work — server handles expansions internally.
  The groups are for query planning guidance, not required.

## Related

- `component-mappings.md` — Canonical component name aliases (via MCP tool)
- `smart-query.md` — Full HyDE + multi-query strategy
