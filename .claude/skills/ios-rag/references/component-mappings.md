# iOS RAG — Component Mappings

Live data served by RAG server. Call the `expansions` MCP tool to get current mappings.

## How to Get Mappings

Call `expansions` tool (format: "full") on the ios-rag MCP server.
Returns `component_mappings` dict: alias (lowercase) → canonical name.

## Usage

- Use canonical name in `component` filter: `filters={"component": "UIButton"}`
- Server handles alias detection automatically
- Do NOT generate synonym variants in queries — server expands them

## Notes

- iOS server uses word-by-word matching only (no phrase matching)
- CamelCase/PascalCase aliases ARE matched as single tokens since they have no spaces
- Multi-word aliases (e.g., "primary button") won't match as phrases — use CamelCase equivalents
- If MCP tool unavailable, queries still work — server handles expansions internally.
  The mappings are for query planning guidance, not required.
