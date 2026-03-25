# Platform Token Mapping

## Overview

Vien 2.0 Figma variables translate to platform-native formats:

| Layer | Web (CSS) | iOS (Swift) | Android (Kotlin) |
|-------|-----------|-------------|------------------|
| Primitives | `--color-blue-500` | `EpostColors.blue500` | `EpostTheme.colors.blue500` |
| Semantic | `--color-primary` | `EpostColors.primary` | `EpostTheme.colors.primary` |
| Component | `--button-bg` | `EpostButton.backgroundColor` | `EpostTheme.button.backgroundColor` |

## Web: CSS Custom Properties

```css
:root {
  --color-primary: var(--color-blue-500);
  --spacing-md: var(--spacing-16);
}
```

Source: `libs/klara-theme/` (React + Tailwind CSS)

## iOS: Swift Constants

```swift
// EpostTheme.swift
public enum EpostColors {
    public static let primary = Color("primary")
    public static let blue500 = Color(hex: "#2563eb")
}
```

Source: `ios_theme_ui/` (SwiftUI)
Note: Token APIs are forward-looking — patterns will evolve as library matures.

## Android: Kotlin Theme Objects

```kotlin
// EpostTheme.kt
object EpostTheme {
    val colors = EpostColors(
        primary = Color(0xFF2563EB),
        blue500 = Color(0xFF2563EB),
    )
}
```

Source: `android_theme_ui/theme/` (Jetpack Compose)
Note: Token APIs are forward-looking — patterns will evolve as library matures.

## Translation Rules

1. **Figma variable name** → kebab-case (web), camelCase (iOS/Android)
2. **Collection** → namespace (web: CSS scope, iOS: enum, Android: object)
3. **Mode** → theme variant (web: class swap, iOS: ColorScheme, Android: isSystemInDarkTheme)
4. **Reference chain** → resolved at build time per platform
