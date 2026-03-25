---
name: knowledge/android-theme/design-system
description: "Android theme design tokens and guidelines"
---

> **Staleness warning**: Manual snapshot. Verify against actual source before relying
> on specific values. Last verified: 2026-02-10.
> When Android RAG is available, convert to pointer file.

# Android Theme Design System

## Token Access

Tokens via Compose theme:
```kotlin
val colors = EpostTheme.colors
val typography = EpostTheme.typography
val spacing = EpostTheme.spacing

Text(
    text = "Hello",
    color = colors.primary,
    style = typography.body
)
```

## Colors

| Token | Usage |
|-------|-------|
| EpostTheme.colors.primary | Primary actions |
| EpostTheme.colors.secondary | Secondary text |
| EpostTheme.colors.success | Success states |
| EpostTheme.colors.error | Error states |
| EpostTheme.colors.background | Screen backgrounds |
| EpostTheme.colors.surface | Card backgrounds |

## Spacing

```kotlin
Modifier.padding(EpostTheme.spacing.md) // 16.dp
Modifier.padding(horizontal = EpostTheme.spacing.lg) // 24.dp
```

## Typography

| Token | Size | Weight |
|-------|------|--------|
| typography.caption | 12sp | Normal |
| typography.body | 16sp | Normal |
| typography.headline | 20sp | SemiBold |
| typography.title | 24sp | Bold |
