# Agent Development for Claude Code Plugins

## Overview

Agents are autonomous subprocesses that handle complex, multi-step tasks independently. Understanding agent structure, triggering conditions, and system prompt design enables creating powerful autonomous capabilities.

**Key concepts:**
- Agents are FOR autonomous work, commands are FOR user-initiated actions
- Markdown file format with YAML frontmatter
- Triggering via description field with examples
- System prompt defines agent behavior
- Model and color customization

## Agent File Structure

### Complete Format

```markdown
---
name: agent-identifier
description: Use this agent when [triggering conditions]. Examples:

<example>
Context: [Situation description]
user: "[User request]"
assistant: "[How assistant should respond and use this agent]"
<commentary>
[Why this agent should be triggered]
</commentary>
</example>

<example>
[Additional example...]
</example>

model: inherit
color: blue
tools: ["Read", "Write", "Grep"]
"
---

You are [agent role description]...

**Your Core Responsibilities:**
1. [Responsibility 1]
2. [Responsibility 2]

**Analysis Process:**
[Step-by-step workflow]

**Output Format:**
[What to return]
```

## Frontmatter Fields

### name (required)

Agent identifier used for namespacing and invocation.

**Format:** lowercase, numbers, hyphens only
**Length:** 3-50 characters
**Pattern:** Must start and end with alphanumeric

**Good examples:**
- `code-reviewer`
- `test-generator`
- `api-docs-writer`
- `security-analyzer`

**Bad examples:**
- `helper` (too generic)
- `-agent-` (starts/ends with hyphen)
- `my_agent` (underscores not allowed)
- `ag` (too short, < 3 chars)

### description (required)

Defines when Claude should trigger this agent. **This is the most critical field.**

**Must include:**
1. Triggering conditions ("Use this agent when...")
2. Multiple `<example>` blocks showing usage
3. Context, user request, and assistant response in each example
4. `<commentary>` explaining why agent triggers

**Best practices:**
- Include 2-4 concrete examples
- Show proactive and reactive triggering
- Cover different phrasings of same intent
- Explain reasoning in commentary
- Be specific about when NOT to use the agent

### model (required)

Which model the agent should use.

**Options:**
- `inherit` - Use same model as parent (recommended)
- `sonnet` - Claude Sonnet (balanced)
- `opus` - Claude Opus (most capable, expensive)
- `haiku` - Claude Haiku (fast, cheap)

**Recommendation:** Use `inherit` unless agent needs specific model capabilities.

### color (required)

Visual identifier for agent in UI.

**Options:** `blue`, `cyan`, `green`, `yellow`, `magenta`, `red`

**Guidelines:**
- Blue/cyan: Analysis, review
- Green: Success-oriented tasks
- Yellow: Caution, validation
- Red: Critical, security
- Magenta: Creative, generation

### tools (optional)

Restrict agent to specific tools.

**Common tool sets:**
- Read-only analysis: `["Read", "Grep", "Glob"]`
- Code generation: `["Read", "Write", "Grep"]`
- Testing: `["Read", "Bash", "Grep"]`
- Full access: Omit field

## System Prompt Design

The markdown body becomes the agent's system prompt. Write in second person, addressing the agent directly.

**DO:** Write in second person, be specific about responsibilities, provide step-by-step process, define output format, keep under 10,000 characters

**DON'T:** Write in first person, be vague, omit process steps, leave output format undefined

## Ecosystem Fields (ePost Agent Kit)

**`skills`** — Array of skill IDs the agent should load. Use bracket notation: `skills: [core, debugging]`. Most agents include `core` as the first skill.

**`memory`** — Controls agent memory scope. Use `project` for agents that need cross-session context.

**`permissionMode`** — Controls what actions the agent can take without user confirmation:
- `default` — Standard permission prompts (most agents)
- `acceptEdits` — Auto-accept file edits (implementers, developers)
- `plan` — Read-only exploration, no writes (architects, researchers, reviewers)
- `bypassPermissions` — Full autonomy (use with caution)

**`disallowedTools`** — Explicitly prevent agent from using specific tools. Useful for read-only agents: `disallowedTools: [Write, Edit]`.

## Declaring a Data Store

If your agent produces persistent project-level data (findings, benchmarks, patches), declare a data domain using the `.epost-data/` convention.

**Template — add to agent system prompt body:**

```markdown
## Data Store
- **DB:** `.epost-data/{domain}/{domain}.json` (if exists)
- **Artifacts:** `.epost-data/{domain}/artifacts/` (if exists)
- **Schema:** `.claude/assets/{domain}-schema.json`
```

**Steps:**
1. Create schema at `packages/{pkg}/assets/{domain}-schema.json`
2. Add the "Data Store" section above to the agent's system prompt
3. Reference paths in commands with `(if exists)` guards

## References

- `references/agent-creation-guide.md` — Full creation workflow, validation rules, testing, organization
