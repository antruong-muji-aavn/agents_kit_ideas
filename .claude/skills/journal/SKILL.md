---
name: journal
description: (ePost) Use after completing a significant feature phase, resolving a non-trivial bug, or making a key architectural decision — writes a structured journal entry to docs/journal/ for future reference
user-invocable: false
metadata:
  keywords:
    - journal
    - decision-log
    - post-mortem
    - implementation-history
    - lessons-learned
  agent-affinity:
    - epost-fullstack-developer
    - epost-debugger
    - epost-planner
  platforms:
    - all
  connections:
    enhances: [knowledge-capture]
---

# Journal Skill

Write implementation history entries to `docs/journal/` after significant work. See `docs/journal/README.md` for epic naming conventions.

## When to Write

| Condition | Write? |
|-----------|--------|
| Non-trivial feature phase completed | Yes |
| Hard bug resolved (surprising root cause, >1 investigation step) | Yes |
| Key architectural decision or rejected alternative | Yes |
| Trivial fix, typo, routine task | No |

## File Path

```
docs/journal/{epic}/{filename}.md
```

**Epic**: plan slug or feature domain — e.g., `skill-discovery/`, `web-platform/`, `a11y/`, `kit/`, `design-system/`

**Filename per agent:**

| Agent | Filename |
|-------|----------|
| `epost-fullstack-developer` | `YYMMDD-{feature-slug}.md` |
| `epost-debugger` | `YYMMDD-fix-{bug-slug}.md` |
| `epost-planner` | `YYMMDD-decision-{slug}.md` |

## Entry Template

```markdown
# {Title}

**Date**: YYYY-MM-DD
**Agent**: epost-{name}
**Epic**: {epic-slug}
**Plan**: plans/{plan-slug}/ (if applicable)

## What was implemented / fixed
{concise description}

## Key decisions and why
- **Decision**: {what was decided}
  **Why**: {rationale, trade-offs considered}

## What almost went wrong
{risks encountered, near-misses, gotchas to remember}
```

## Rules

- Skip the entry if the task was trivial — journal should signal importance
- Keep entries concise; this is a reference artifact, not prose
- Use the same epic slug across related entries to group them
- Create the epic directory if it doesn't exist yet
- **Signal emission**: In "What almost went wrong", if a skill gap caused the issue or a workaround was needed, name the skill explicitly. Format: `[skill-name] did not cover X` or `workaround: used Y instead of X (skill-name should handle this)`
