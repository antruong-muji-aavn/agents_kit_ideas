# UI Guidance — Integration Consulting & Conflict Resolution

Reference for epost-muji when providing cross-platform integration guidance to feature teams.
Loaded via `ui-lib-dev` skill when context is consumer guidance (not audit).

Also applies during audit when providing fix recommendations — the conflict resolution table guides remediation suggestions.

## When to Apply

- Team asks how to integrate a UI component
- Design-code mismatch found during review or audit
- Token usage questions (which token for which purpose)
- Component customization beyond standard variants
- Platform-specific implementation differences
- Responsive behavior questions

## Guidance Workflow

### 1. Understand Context
- What platform? (web/iOS/Android)
- What component or pattern?
- What's the design spec (Figma link or screenshot)?
- What's the current implementation?

### 2. Diagnose
- Compare implementation against design tokens (load KB: `libs/klara-theme/docs/index.json`)
- Check component API usage against FEAT-* entry for the component
- Identify token misuse (hardcoded values, wrong semantic level)
- Flag accessibility gaps

### 3. Resolve Design-Code Conflicts

| Conflict | Resolution |
|----------|-----------|
| Design uses non-existent token | Map to closest semantic token; flag for design team to add the token |
| Design spacing doesn't match scale | Use nearest scale value; document deviation in PR |
| Component doesn't support variant | Extend via composition, not fork; propose variant to MUJI team if recurring |
| Platform limitation prevents exact match | Document acceptable deviation; prioritize design intent over pixel-perfect |
| Dark mode breaks contrast | Use semantic tokens (auto-adjusts via theme layer); validate contrast ratio |
| Embedded component style mismatch | Use `className` prop if available; never target internal CSS classes |

### 4. Platform-Specific Knowledge

Load platform ui-lib skill via skill-discovery:
- Web: `web-ui-lib` (React/klara-theme patterns)
- iOS: `ios-ui-lib` (SwiftUI/ios_theme_ui patterns)
- Android: `android-ui-lib` (Compose/android_theme_ui patterns)

### 5. Review Checklist (Consumer Guidance)

When reviewing UI code for another team (not a formal audit):

- [ ] Uses design tokens (no hardcoded colors/spacing/typography)
- [ ] Correct semantic level (not using primitive tokens directly)
- [ ] Component from library (not custom recreation of existing component)
- [ ] Responsive behavior matches design intent
- [ ] Dark mode supported via semantic tokens
- [ ] Accessibility: contrast, touch targets, labels
- [ ] Platform conventions followed (HIG/Material/Web standards)

## Integration with Audit

When invoked via `/audit --ui`, follow the audit skill's UI mode first. Use this file for:
1. Generating fix suggestions for findings (conflict resolution table)
2. Answering "how should I fix this?" follow-up questions
3. Platform-specific remediation guidance
