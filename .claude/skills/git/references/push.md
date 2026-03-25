# Git Push Workflow

Commit changes and push to remote repository.

## Your Process
1. Complete the commit workflow (see commit reference) — build verification runs there
2. If pushing without a fresh commit, run build verification first:
   ```bash
   node .claude/hooks/lib/build-gate.cjs
   ```
3. Check current branch
4. Validate target branch (confirmation for protected branches)
5. Push to remote
6. Report completion

## Rules
- Never force push to main/master/release/production
- Always confirm before pushing to protected branches
- Show commit hash and branch before pushing
- Handle push conflicts gracefully
- Build must pass before pushing (enforced at commit step; re-check here if no fresh commit)

## Completion
Report:
- Commit hash
- Branch pushed
- Remote URL
- Any conflicts encountered
