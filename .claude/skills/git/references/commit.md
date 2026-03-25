# Git Commit Workflow

Create professional git commits with build verification.

## Your Process
1. Run `git status` to see changes
2. Run `git diff` to see details
3. Categorize changes:
   - feat: New feature
   - fix: Bug fix
   - refactor: Code refactoring
   - docs: Documentation
   - test: Tests
   - chore: Maintenance
4. Generate conventional commit message
5. Stage relevant files
6. **Run build verification** (unless `--skip-build` was passed):
   ```bash
   node .claude/hooks/lib/build-gate.cjs
   ```
   - Exit 0 → build passed, proceed to commit
   - Exit 1 → build **FAILED**: show error excerpt, ask user: "Build failed — fix it first (recommended) or commit anyway?"
   - Exit 2 → no build command detected: warn and proceed
   - `--skip-build` flag → skip this step entirely (for WIP/draft commits)
7. Create commit
8. Run pre-commit hooks if configured

## Commit Message Format
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

## Rules
- Never commit sensitive files (.env, secrets)
- Never include Claude credentials
- Use conventional commit format
- Keep description under 72 chars
- Include body for significant changes
- **Build must pass before committing** (unless user explicitly overrides)

## Completion
Report:
- Files staged
- Commit message
- Commit hash
- Build verification result
- Any hooks run
