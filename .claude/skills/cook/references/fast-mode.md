# Cook Fast Mode

Direct implementation — skip "Should I create a plan first?" question, implement immediately.

<feature>$ARGUMENTS</feature>

**IMPORTANT:** Analyze the skills catalog and activate the skills that are needed for the task.

## Step 1: Intent Classification

Classify the feature before writing any code:

| Dimension | Value |
|-----------|-------|
| **Type** | feature / fix / refactor / docs |
| **Scope** | single-file / multi-file / multi-module |
| **Risk** | low / medium / high (schema changes, auth, public APIs) |

**Guard rail**: If scope is multi-module (>5 files likely), pause and suggest `/cook --parallel` instead.

## Step 2: Implement

Create/modify files directly (no plan creation).

- Report progress per file as you go
- Follow YAGNI, KISS, DRY principles
- Respect file ownership — don't modify files outside the feature scope

### Batch Checkpoint Protocol

After every 3 file modifications (create or edit):
1. Run type check on modified files (if type checker available)
2. Run lint on modified files (if linter available)
3. If pass: log `Checkpoint {N}: {file1}, {file2}, {file3} — PASS` and continue
4. If fail: fix immediately before modifying the next file

**Exceptions**: Documentation-only changes (`.md` files) skip checkpoints.
**Counter**: Resets per phase, not per session. Only code files count.
**Fallback**: If no type checker or linter configured, verify files are syntactically valid (e.g., `node -c` for JS/CJS).

## Step 3: Review Gate

After implementation, run all checks before testing:

1. **Type Check** — No compilation errors
2. **Lint** — No lint violations

If any check fails → fix immediately before proceeding.

## Step 4: Test

Write and run tests for new code.

- Unit tests for new functions/methods
- Integration test if touching external boundaries (API, DB, auth)
- All relevant tests must pass

**Auto-escalation**: Follow `error-recovery` mutation discipline — each retry MUST use a different approach. After 2 different approaches fail → escalate to `epost-debugger` with the attempt log and failing test output. Do not attempt a third guess.

## Step 5: Finalize

1. **Status update** — If working from a plan, update `{plan_dir}/status.md`:
   - Progress table: mark completed phase as `Done`
   - Key Decisions: add any significant choices made during implementation
   - Architecture Reference: note any discovered structure
2. **Docs update** — Update relevant docs if public API or behavior changed
3. **Change summary** — Output a concise summary:
   ```
   Files changed: N
   Tests added: N
   Behavior change: [yes/no + 1 line description]
   Follow-up tasks: [any new issues discovered]
   ```
3. **Commit offer** — Ask: "Commit? [Y/n]" → use `epost-git-manager` if yes

## Rules

- Always write tests for new code
- Never skip the Review Gate (Step 3)
- Never attempt the same fix more than twice — escalate instead
