---
name: knowledge/android-theme/components
description: "Android theme component catalog"
---

> **Staleness warning**: Manual snapshot. Verify against actual source before relying
> on specific values. Last verified: 2026-02-10.
> When Android RAG is available, convert to pointer file.

# Android Theme Components

## Architecture

Android theme library built on:
- **Jetpack Compose** with Material 3 foundation
- **Gradle** distribution (internal Maven repo)
- **CompositionLocal** for token propagation

## Core Components

### Layout
- `EpostCard` — Themed card with elevation
- `EpostSurface` — Background surface

### Forms
- `EpostTextField` — Material text field with epost styling
- `EpostButton` — Primary button (Primary, Secondary, Ghost variants)
- `EpostCheckbox` / `EpostRadio` — Themed selection controls

### Data Display
- `EpostBadge` — Status indicator
- `EpostAvatar` — Profile image
- `EpostTag` — Label component

## Usage

```kotlin
import no.epost.theme.compose.*

@Composable
fun MyScreen() {
    EpostCard {
        EpostTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name"
        )
        EpostButton(
            text = "Submit",
            style = EpostButtonStyle.Primary,
            onClick = { submit() }
        )
    }
}
```
