---
name: kit-add-hook
description: "(ePost) Create a new hook for Claude Code automation"
user-invocable: false
disable-model-invocation: true
metadata:
  argument-hint: "[hook-name] [event-type]"
  connections:
    requires: []
---

## Your Mission

Create a new hook script and wire it into the settings.json hook configuration.

**IMPORTANT:** Read `kit/references/hooks.md` for hook patterns and best practices.

## Arguments

- HOOK_NAME: $1 (required — kebab-case, e.g. `my-validator`)
- EVENT_TYPE: $2 (optional — PreToolUse, PostToolUse, SessionStart, Stop, UserPromptSubmit)

## Workflow

1. **Gather Info** (if not provided via arguments):
   - Hook name and purpose
   - Hook event type (PreToolUse, PostToolUse, Stop, SessionStart, UserPromptSubmit, etc.)
   - Hook type: command (deterministic) or prompt (LLM-driven)
   - Tool matcher (for PreToolUse/PostToolUse): which tools to match
   - Which package it belongs to (usually `core`)

2. **Create Hook Script**:
   - Create `packages/{package}/hooks/{hook-name}.cjs`
   - Use CommonJS format (`.cjs`) for Node.js hooks
   - Read JSON from stdin, write JSON to stdout
   - Handle exit codes: 0 = success, 2 = blocking error
   - Follow security best practices (validate input, quote vars)

3. **Wire into Settings**:
   - Edit `packages/core/settings.json` to add hook under appropriate event
   - Set matcher pattern for tool-specific hooks
   - Set appropriate timeout

4. **Update Package Files**:
   - Add hook file to `packages/{package}/package.yaml` files mapping if needed
   - Ensure hook directory is included in files mapping

5. **Test**:
   - Pipe sample JSON to hook: `echo '{"tool_name":"X"}' | node packages/{package}/hooks/{hook-name}.cjs`
   - Verify JSON output and exit code

6. **Regenerate**: Run `epost-kit init --fresh`

7. **Report**: Hook name, event, matcher, file paths, test results

## Hook Script Template

```javascript
#!/usr/bin/env node
'use strict';

const fs = require('fs');
const input = JSON.parse(fs.readFileSync('/dev/stdin', 'utf8'));

// Extract relevant fields based on event type
const toolName = input.tool_name || '';
const toolInput = input.tool_input || {};

// Hook logic here
const shouldBlock = false; // Replace with actual logic

if (shouldBlock) {
  process.stderr.write(JSON.stringify({
    hookSpecificOutput: { permissionDecision: 'deny' },
    systemMessage: 'Blocked: reason here'
  }));
  process.exit(2);
}

// Success — optionally inject context
console.log(JSON.stringify({
  systemMessage: 'Hook passed'
}));
```

## Rules

- Use `.cjs` extension for Node.js hooks (CommonJS)
- Always validate JSON input before processing
- Set appropriate timeouts (10s for fast checks, 60s max)
- Source of truth is `packages/`, NOT `.claude/`
- Test hooks locally before wiring into settings
