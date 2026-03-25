---
name: kit-add-skill
description: "(ePost) Create a new skill definition"
user-invocable: false
disable-model-invocation: true
metadata:
  argument-hint: "[skill-name] [description]"
  connections:
    requires: []
---

## Your Mission

Create a new skill definition following epost_agent_kit conventions.

**IMPORTANT:** Read `kit/references/skill-development.md` for frontmatter reference and best practices.

## Arguments

- SKILL_NAME: $1 (required — lowercase, hyphens, e.g. `my-skill`)
- DESCRIPTION: $2 (optional — what the skill teaches)

## Workflow

1. **Gather Info** (if not provided via arguments):
   - Skill name and domain category
   - Purpose — what development task it teaches
   - Which package it belongs to (core, platform-web, platform-ios, platform-android, platform-backend, meta-kit-design, ui-ux)
   - Whether it's user-invocable (slash command) or background (passive knowledge)
   - Whether it needs `context: fork` (task-oriented) or default (passive)

2. **Suggest Connections** — scan existing skills for likely relationships:
   - `extends`: is this a specialization of an existing skill? (e.g., `ios-a11y` extends `a11y`)
   - `requires`: does it depend on another skill to function?
   - `enhances`: does it complement another skill?
   - `conflicts`: is it mutually exclusive with another?

3. **Scaffold Skill Directory**:
   - Create `packages/{package}/skills/{skill-name}/SKILL.md` with proper frontmatter
   - Include: name, description (with trigger phrases)
   - Set: user-invocable, context, agent, allowed-tools as needed
   - Add `metadata.connections` if relationships identified in step 2
   - Write concise SKILL.md body — quick reference, NOT documentation
   - Create `references/` directory if the skill needs detailed reference files

4. **Progressive Disclosure**:
   - `SKILL.md` — short, concise (< 100 lines), always loaded
   - `references/*.md` — detailed patterns, loaded on demand via Read tool
   - Token efficiency is critical — keep SKILL.md lean

5. **Copy to Package Source**:
   - Copy skill directory to `packages/{package}/skills/{category}/{skill-name}/`
   - This is the source of truth — `.claude/` is generated output

6. **Register**:
   - Update `packages/{package}/package.yaml` skills list

7. **Validate**: Run `epost-kit lint` on new skill — catch broken refs and connection issues

8. **Report**: Skill name, package, files created, trigger phrases, connections

## Post-Creation Checklist

- [ ] Frontmatter has name and description with trigger phrases
- [ ] `metadata.keywords` present (min 3)
- [ ] `metadata.platforms` set (not defaulting to "all" unless intentional)
- [ ] `metadata.connections` declared if obvious parent/dependency exists
- [ ] Registered in package.yaml `provides.skills`
- [ ] No lint errors (`epost-kit lint`)

## Rules

- Skills are NOT documentation — they teach Claude HOW to perform tasks
- Each skill teaches a specific development workflow, not what a tool does
- Use progressive disclosure: SKILL.md is lean, references/ has details
- `version:` is NOT a valid frontmatter field
- Background skills use `user-invocable: false`
