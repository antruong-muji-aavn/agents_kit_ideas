---
name: knowledge/ios-theme/design-system
description: "iOS theme design tokens and guidelines"
---

> **Staleness warning**: Manual snapshot. Verify against actual source before relying
> on specific values. Last verified: 2026-02-10.
> When iOS RAG is available, convert to pointer file.

# iOS Theme Design System

## Token Access

Tokens are accessed via SwiftUI Environment:
```swift
@Environment(\.epostTheme) var theme

Text("Hello")
    .foregroundColor(theme.colors.primary)
    .font(theme.typography.body)
```

## Colors

| Token | Usage |
|-------|-------|
| theme.colors.primary | Primary actions |
| theme.colors.secondary | Secondary text |
| theme.colors.success | Success states |
| theme.colors.warning | Warning states |
| theme.colors.error | Error states |
| theme.colors.background | View backgrounds |
| theme.colors.surface | Card backgrounds |

## Spacing

Uses the same scale as web: 4, 8, 12, 16, 24, 32, 48

```swift
.padding(theme.spacing.md) // 16pt
.padding(.horizontal, theme.spacing.lg) // 24pt
```

## Typography

| Token | Size | Weight |
|-------|------|--------|
| theme.typography.caption | 12pt | Regular |
| theme.typography.body | 16pt | Regular |
| theme.typography.headline | 20pt | Semibold |
| theme.typography.title | 24pt | Bold |
