---
name: knowledge/ios-theme/components
description: "iOS theme component catalog"
---

> **Staleness warning**: Manual snapshot. Verify against actual source before relying
> on specific values. Last verified: 2026-02-10.
> When iOS RAG is available, convert to pointer file.

# iOS Theme Components

## Architecture

iOS theme library built on:
- **SwiftUI** (primary) with UIKit bridging
- **Swift Package Manager** distribution
- **Design tokens** via SwiftUI Environment

## Core Components

### Layout
- `EpostStack` — VStack/HStack wrapper with design system spacing
- `EpostCard` — Rounded container with elevation
- `EpostDivider` — Themed divider

### Navigation
- `EpostNavigationBar` — Custom navigation bar
- `EpostTabBar` — Bottom tab navigation

### Forms
- `EpostTextField` — Styled text input with validation
- `EpostPicker` — Themed picker
- `EpostToggle` — Branded toggle switch

### Data Display
- `EpostBadge` — Status indicator
- `EpostAvatar` — Profile image component
- `EpostTag` — Label component

### Actions
- `EpostButton` — Primary button with variants (.primary, .secondary, .ghost)
- `EpostIconButton` — Icon-only button

## Usage

```swift
import EpostThemeUI

struct MyView: View {
    var body: some View {
        EpostCard {
            EpostTextField("Name", text: $name)
            EpostButton("Submit", style: .primary) {
                submit()
            }
        }
    }
}
```
