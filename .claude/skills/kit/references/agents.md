# Agent Ecosystem Reference

Persistent reference for the epost_agent_kit Claude Code ecosystem. Use this when planning changes, creating new components, or auditing the agent system.

## Claude Code Official Components

| Component | Location | Auto-discovered | Purpose |
|-----------|----------|----------------|---------|
| **Agents** | `.claude/agents/` | Yes | Autonomous subprocesses for complex tasks |
| **Skills** | `.claude/skills/` | Yes (nested) | Domain knowledge and workflows |
| **Commands** | `.claude/commands/` | Yes (nested) | User-triggered slash commands (merged into skills) |
| **Hooks** | `.claude/settings.json` | Yes | Event-triggered actions (PreToolUse, PostToolUse, Stop, etc.) |
| **Output Styles** | `.claude/output-styles/` | Yes | Custom response formatting |
| **MCP Servers** | `.mcp.json` (project) / `settings.json` (user) | Yes | External tool integrations |
| **Plugins** | `.claude-plugin/plugin.json` | Via `--plugin-dir` | Distributable component bundles |

**NOT official:** `.claude/workflows/` — Custom reference documentation only, not auto-discovered by Claude Code.

## Commands & Skills Merge

Commands and skills are functionally equivalent and share the same frontmatter fields. Both create `/slash-command` entries. Skills are the preferred format for new additions. Commands in `.claude/commands/` continue to work.

**Key difference:** Skills use `SKILL.md` in a directory; commands are standalone `.md` files in `commands/`.

## Nested Directory Support

Skills support nested directories. Claude Code auto-discovers `SKILL.md` files at any depth under `.claude/skills/`.

## Agent Frontmatter Reference

| Field | Required | Source | Type | Description |
|-------|----------|--------|------|-------------|
| `name` | Yes | Official | string | Identifier (lowercase, hyphens, 3-50 chars) |
| `description` | Yes | Official | string | Triggering conditions with `<example>` blocks |
| `model` | Yes | Official | string | `inherit`, `sonnet`, `opus`, `haiku` |
| `color` | Yes | Official | string | `blue`, `cyan`, `green`, `yellow`, `magenta`, `red` |
| `tools` | No | Official | array | Restrict available tools (default: all) |
| `skills` | No | Ecosystem | array | Preload skills into agent context at startup |
| `memory` | No | Ecosystem | string | Persistent learning: `user`, `project`, `local` |
| `permissionMode` | No | Ecosystem | string | `default`, `acceptEdits`, `dontAsk`, `bypassPermissions`, `plan` |
| `hooks` | No | Ecosystem | object | Per-agent scoped hooks (same format as settings.json hooks) |
| `disallowedTools` | No | Ecosystem | string | Comma-separated tools to deny (advisory) |
| `allowedTools` | No | Ecosystem | string | Comma-separated tools to allow |

**Source legend:** *Official* = confirmed in upstream `anthropics/claude-code` plugin-dev docs. *Ecosystem* = documented in our reference and observed working but not confirmed upstream.

## Skill Frontmatter Reference

| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `name` | Yes | string | Skill identifier |
| `description` | Yes | string | When to use (third-person with trigger phrases) |
| `context` | No | string | `fork` — run in isolated subagent |
| `agent` | No | string | Which subagent type for `context: fork` |
| `user-invocable` | No | boolean | `false` hides from `/` menu |
| `disable-model-invocation` | No | boolean | `true` prevents auto-trigger |
| `allowed-tools` | No | array | Restrict tools in forked context |
| `hooks` | No | object | Per-skill hooks |
| `model` | No | string | Override model for this skill |
| `argument-hint` | No | string | Placeholder text for skill argument |

**epost-kit metadata extensions (nested under `metadata:`):**

| Field | Type | Description |
|-------|------|-------------|
| `keywords` | array | Search terms for skill discovery |
| `platforms` | array | Target platforms: ios, android, web, backend, cli, all |
| `triggers` | array | File patterns that trigger skill (e.g., `.kt`, `.swift`) |
| `agent-affinity` | array | Preferred agents for this skill |
| `connections.extends` | array | Parent skills this inherits from |
| `connections.requires` | array | Skills that must be co-loaded |
| `connections.conflicts` | array | Skills that cannot coexist |
| `connections.enhances` | array | Optional complementary skills |

**Invalid fields:** `version` (not recognized by Claude Code)

## Naming Conventions

| Component | Pattern | Example |
|-----------|---------|---------|
| Agent files | `epost-<role>.md` | `epost-planner.md` |
| Commands | `<category>/<action>.md` | `web/cook.md` |
| Skills | `<name>/SKILL.md` | `web-nextjs/SKILL.md` |

## Plugin System

Distributable via `.claude-plugin/plugin.json`:

```json
{
  "name": "plugin-name",
  "description": "Plugin description",
  "version": "1.0.0",
  "commands": ["./.claude/commands/"],
  "agents": ["./.claude/agents/"],
  "skills": ["./.claude/skills/"]
}
```

Load with: `claude --plugin-dir /path/to/plugin`
