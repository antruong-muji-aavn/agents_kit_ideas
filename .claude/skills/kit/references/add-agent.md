---
name: kit-add-agent
description: "(ePost) Create a new agent definition"
user-invocable: false
disable-model-invocation: true
metadata:
  argument-hint: "[agent-name] [description]"
  connections:
    requires: []
---

## Your Mission

Create a new agent definition file following epost_agent_kit conventions.

**IMPORTANT:** Read `kit/references/agent-development.md` for frontmatter reference and best practices.

## Arguments

- AGENT_NAME: $1 (required — lowercase, hyphens, follows `epost-<role>` pattern)
- DESCRIPTION: $2 (optional — what the agent does)

## Workflow

1. **Gather Info** (if not provided via arguments):
   - Agent name (must follow `epost-<role>` pattern)
   - Purpose / what tasks it handles
   - Which package it belongs to (core, platform-web, platform-ios, platform-android, platform-backend, meta-kit-design, ui-ux)
   - Model tier: haiku (routing/search), sonnet (implementation), opus (architecture)
   - Color: blue, cyan, green, yellow, magenta, red

2. **Suggest Skills** — scan existing skills for likely matches based on agent role/platform:
   - Core skills all agents should have (e.g., `core`)
   - Platform skills matching the agent's domain
   - Workflow skills matching the agent's purpose

3. **Scaffold Agent File**:
   - Create `packages/{package}/agents/{agent-name}.md` with proper frontmatter
   - Include: name, description (with `<example>` blocks), model, color
   - Add: skills (from step 2), permissionMode, disallowedTools as needed
   - Write system prompt body with role definition, workflow, and rules

4. **Copy to Package Source**:
   - Copy agent file to `packages/{package}/agents/{agent-name}.md`
   - This is the source of truth — `.claude/` is generated output

5. **Register**:
   - Update `packages/{package}/package.yaml` agents list
   - Update `packages/kit/skills/kit/references/agents.md` agent tree

6. **Validate**: Run `epost-kit lint` on new agent — catch broken skill refs

7. **Report**: Agent name, package, model, skills list, file paths created

## Rules

- Agent names MUST start with `epost-`
- Description MUST include `<example>` blocks for triggering
- Agents that write reports/plans use `permissionMode: default` + instruction-level "do not modify source code" constraint
- `permissionMode: plan` blocks ALL writes including reports — only use for truly read-only agents with no file output
- Follow YAGNI — don't add tools/skills the agent won't use
