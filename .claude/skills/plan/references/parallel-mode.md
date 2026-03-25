---
name: plan-parallel
description: "(ePost) Dependency-aware plan with file ownership matrix for parallel execution"
user-invocable: false
disable-model-invocation: true
metadata:
  argument-hint: "[enhanced planning prompt from router]"
  connections:
    extends: [plan]
    conflicts: [plan-fast, plan-deep]
---

# Plan Parallel Variant

Comprehensive planning with file ownership tracking and dependency graph for parallel agent execution.

## When to Use
- Multi-module features (DB + API + UI)
- Features with independent subsystems
- Tasks parallelizable across teams/agents
- Complexity score 4-5 (router auto-routes complex multi-module tasks)

## Execution Steps

### 1-4. Same as /plan-deep
Follow `plan-deep` steps 1 through 5 (including step 1.5):
- Parse enhanced prompt
- Check codebase summary
- Create plan directory
- Sequential research (2 researchers)
- Aggregate research

### 5. ADDITIONAL: Decomposition Analysis

After research aggregation, perform file ownership analysis:

**Identify ALL Files**:
```
From both research reports + codebase analysis:
- List every file to CREATE
- List every file to MODIFY
- List every file READ-ONLY (used but not modified)
```

**Assign Exclusive Ownership**:
```
For each file in CREATE or MODIFY lists:
  1. Determine which phase owns this file
  2. Rule: ONE phase per file (no sharing)
  3. If conflict: assign to EARLIEST phase number
  4. Later phases become BLOCKED BY earlier phase
```

**Build Dependency Graph**:
```
For each phase:
  1. Identify data dependencies (what this phase needs from others)
  2. Identify API contract dependencies (what interfaces this phase provides/consumes)
  3. Build phase DAG (Directed Acyclic Graph)
  4. Group into parallel execution batches
```

**Validate**:
```
Check:
- No file appears in multiple phases' CREATE/MODIFY lists
- Dependency graph has no cycles (must be DAG)
- Max 7 phases (prevent over-decomposition)
- Each file has owner OR is read-only
```

### 6. Generate plan.md with Ownership Matrix

Same as hard.md PLUS these sections:

**File Ownership Matrix** (after Research Summary):
```markdown
## File Ownership Matrix

| File Path | Owner Phase | Operation | Notes |
|-----------|-------------|-----------|-------|
| `path/to/file.ts` | Phase 02 | Create | New module |
| `path/to/other.ts` | Phase 03 | Modify | Add endpoints |
| `shared/types.ts` | Phase 01 | Create | Shared types |

**Validation**: No file duplicates in Owner Phase column.
```

**Dependency Graph** (after File Ownership Matrix):
```markdown
## Dependency Graph

### Execution Batches

**Batch 1** (parallel):
- Phase 01: {Name} [no dependencies]
- Phase 02: {Name} [no dependencies]

**Batch 2** (parallel, after Batch 1):
- Phase 03: {Name} [depends on: Phase 01, Phase 02]

### Dependency Arrows
Phase 01 ──┐
            ├──▶ Phase 03
Phase 02 ──┘

**DAG Validation**: ✓ No cycles detected
```

**Conflict Resolution Log** (if any conflicts detected):
```markdown
## Conflict Resolution

**Resolved**:
- `file.ts`: Phases 02, 03 both modify → Assigned to Phase 02 (earliest)
  → Phase 03 now depends on Phase 02
```

Max plan.md: 150 lines (110 base + 40 for ownership/graph).

### 6.5. Generate status.md

Create `{plan_dir}/status.md` from `references/status-template.md`:
- Pre-populate Progress table from plan.md phases (all `Pending`)
- Not Yet Started: list all phases
- Key Decisions: pre-populate with research-driven decisions from R1/R2 reports
- Architecture Reference: add file ownership matrix summary:
  ```
  **File Ownership Summary**
  - Phase {N} owns: {files listed in ownership matrix}
  ...

  Execution batches:
  - Batch 1 (parallel): {phases}
  - Batch 2 (parallel, after Batch 1): {phases}
  ```
- Known Bugs: "None currently tracked."

### 7. Generate Phase Files with Parallelization Info

Same as hard.md PLUS Parallelization Info section BEFORE Implementation Steps:

```markdown
## Parallelization Info

- **Execution Batch**: Batch {N}
- **Can Run Parallel With**: Phase {X}, Phase {Y}
- **Blocked By**: Phase {A} (must complete first)
- **Blocks**: Phase {C} (waiting on this)
- **Exclusive Files**:
  - `path/file.ts` (Create)
  - `path/other.ts` (Modify)
- **Shared Read-Only**:
  - `docs/system-architecture.md`
  - `shared/types.ts`

**File Ownership**: This phase has EXCLUSIVE write access to listed files. No other phase modifies these files.
```

Max phase file: 240 lines (200 base + 40 for parallelization).

### 8. Validate Plan (auto-triggered)

Before activating, run a quick validation pass:
1. Load `references/validate-mode.md`
2. Generate 3-5 critical questions about the plan (assumptions, risks, tradeoffs, file ownership)
3. Present to user: "Before I activate this plan, a few quick questions..."
4. Document answers in `plan.md` under `## Validation Summary`

**User can skip**: If user says "skip" or "just activate" → proceed directly to step 9.

### 9. Set Active Plan
```bash
node .claude/scripts/set-active-plan.cjs {plan_path}
```

### 10. Report Completion
```
✓ Parallel Plan Created: {plan_path}

Research:
- R1: Best practices & approaches
- R2: Codebase analysis & dependencies

Decomposition:
- File ownership matrix: {X} files tracked
- Dependency graph: {N} phases in {B} batches
- Conflicts resolved: {C} (see plan.md)

Generated:
- plan.md ({X} lines, includes ownership matrix + graph)
- {N} phase files (with parallelization info)
- 2 research reports

Parallel Execution:
- Batch 1: {phases} (can run in parallel)
- Batch 2: {phases} (after Batch 1)
...

Next Steps:
1. Review ownership: grep "Owner Phase" {plan_path}/plan.md
2. Review dependencies: grep "Depends on" {plan_path}/phase-*.md
3. Execute: /code:parallel {plan_path}

Note: PARALLEL plan with file ownership tracking. Use /code:parallel for parallel execution.
```

## Constraints
- Same as hard.md PLUS:
  - Max 7 phases (prevent over-decomposition)
  - File ownership matrix must be machine-parseable
  - Dependency graph must be human-readable
  - No circular dependencies (DAG only)
  - Parallelization Info in every phase file
  - Plan.md ≤ 150 lines (110 base + 40 ownership)
  - Phase files ≤ 240 lines (200 base + 40 parallelization)

## Conflict Detection Algorithm
```
For each file in all phases' CREATE/MODIFY lists:
  1. Count phases that list this file
  2. If count > 1:
     a. Flag as CONFLICT
     b. Assign to phase with lowest number (earliest)
     c. Add dependency: later phases DEPEND ON earliest
     d. Log in plan.md Conflict Resolution section
```

## Error Handling
- Same as hard.md PLUS:
  - Circular dependency detected: Error, suggest redesign
  - >7 phases: Error, suggest consolidation
  - File overlap unresolvable: Error, manual intervention

## Quality Standards
- Same as hard.md PLUS:
  - Every file has exactly one owner (or none if read-only)
  - Dependency graph is a valid DAG
  - Execution batches are logically correct
  - Parallelization Info matches ownership matrix
  - Conflict resolution documented
