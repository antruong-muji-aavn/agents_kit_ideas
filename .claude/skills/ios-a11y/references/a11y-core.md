---
name: a11y-core
description: Core accessibility principles for iOS — WCAG 2.1 AA, VoiceOver, UIKit/SwiftUI patterns
---

# Accessibility Core Rules

## Purpose

Foundational accessibility rules for iOS VoiceOver support, ensuring all UI elements are accessible and follow WCAG 2.1 AA standards.

## Table of Contents

- [Core Principles](#core-principles)
- [Making Elements Accessible](#making-elements-accessible)
- [Accessibility Properties](#accessibility-properties)
- [VoiceOver Detection](#voiceover-detection)
- [Required Properties](#required-properties)
- [Best Practices](#best-practices)
- [Unreachable Elements Detection](#unreachable-elements-detection)
- [Dynamic Type (UIKit)](#dynamic-type-uikit)
- [SwiftUI Accessibility Modifiers (iOS 15+)](#swiftui-accessibility-modifiers-ios-15)
- [Dynamic Type & Reduce Motion (SwiftUI)](#dynamic-type--reduce-motion)

## Related Documents

- [a11y-buttons](./a11y-buttons.md) - Button-specific accessibility
- [a11y-forms](./a11y-forms.md) - Form input accessibility
- [a11y-headings](./a11y-headings.md) - Heading structure
- [a11y-focus](./a11y-focus.md) - Focus management
- [a11y-images](./a11y-images.md) - Image accessibility
- [a11y-colors-contrast](./a11y-colors-contrast.md) - Color and contrast
- [a11y-testing](./a11y-testing.md) - Testing guidelines

## Core Principles

**WCAG 2.1 AA Requirements:**
- **Perceivable**: UI elements must be detectable by assistive technologies
- **Operable**: All interactive elements must be keyboard/VoiceOver navigable
- **Understandable**: Labels, hints, and values must be clear
- **Robust**: Accessibility properties must be correctly set

**iOS-Specific:**
- All interactive elements must be VoiceOver accessible
- Custom views must explicitly enable accessibility
- Labels must be concise and descriptive
- Hints should explain actions, not repeat labels

## Making Elements Accessible

### Enable Accessibility

**Standard UIKit Controls:**
- Buttons, text fields, labels are accessible by default
- No additional setup required for standard controls

**Custom Views:**
- Must explicitly enable accessibility
- Set `isAccessibilityElement = true` before setting other properties

```swift
// ✅ Correct: Enable accessibility first
customView.isAccessibilityElement = true
customView.accessibilityLabel = "Custom view description"

// ❌ Wrong: Setting label without enabling
customView.accessibilityLabel = "Description" // Won't work
```

### Container Views

**Views containing multiple elements:**
- Set `isAccessibilityElement = false` on container
- Individual child elements handle their own accessibility
- Use `accessibilityContainer` for complex hierarchies

```swift
// Container with multiple interactive elements
containerView.isAccessibilityElement = false
button1.isAccessibilityElement = true
button2.isAccessibilityElement = true
```

## Accessibility Properties

### Label (Required)

**Purpose:** Identifies the element to VoiceOver users

**Rules:**
- Must be concise (1-2 words when possible)
- Describe what the element is, not what it does
- Use sentence case, no trailing punctuation
- Localize all labels

```swift
// ✅ Good labels
button.accessibilityLabel = "Save"
imageView.accessibilityLabel = "Profile photo"
label.accessibilityLabel = "Welcome message"

// ❌ Bad labels
button.accessibilityLabel = "Click here to save your document" // Too verbose
imageView.accessibilityLabel = "Image" // Too generic
label.accessibilityLabel = "Welcome!" // Punctuation unnecessary
```

### Hint (Optional)

**Purpose:** Explains what happens when user interacts with element

**Rules:**
- Only add when action isn't obvious from label
- Start with verb (e.g., "Opens", "Navigates to")
- Keep under 10 words
- Don't repeat information from label

```swift
// ✅ Good hints
deleteButton.accessibilityHint = "Deletes the selected item"
infoButton.accessibilityHint = "Opens help documentation"

// ❌ Bad hints
saveButton.accessibilityHint = "Saves" // Obvious from label
button.accessibilityHint = "This button saves your document when you tap it" // Too verbose
```

### Value (Dynamic Content)

**Purpose:** Describes current state or value of element

**Rules:**
- Use for elements with changing values (sliders, progress bars, toggles)
- Update value when state changes
- Combine with label for complete description

```swift
// ✅ Good value usage
slider.accessibilityLabel = "Volume"
slider.accessibilityValue = "50 percent"

progressView.accessibilityLabel = "Download progress"
progressView.accessibilityValue = "75 percent complete"

toggle.accessibilityLabel = "Dark mode"
toggle.accessibilityValue = isEnabled ? "On" : "Off"
```

### Traits

**Purpose:** Describes element type and behavior to VoiceOver

**Common Traits:**
- `.button` - Interactive button
- `.header` - Section heading
- `.link` - Navigational link
- `.image` - Image element
- `.searchField` - Search input
- `.selected` - Currently selected
- `.adjustable` - Value can be adjusted
- `.notEnabled` - Disabled state

```swift
// ✅ Setting appropriate traits
saveButton.accessibilityTraits = .button
sectionHeader.accessibilityTraits = .header
linkLabel.accessibilityTraits = .link
slider.accessibilityTraits = [.adjustable, .updatesFrequently]

// Multiple traits
customButton.accessibilityTraits = [.button, .selected]
```

## VoiceOver Detection

### Check VoiceOver Status

**Detect if VoiceOver is running:**
- Use `UIAccessibility.isVoiceOverRunning`
- Adjust UI behavior when VoiceOver is active
- Don't rely on this for core accessibility (always set properties)

```swift
if UIAccessibility.isVoiceOverRunning {
    // Adjust UI for VoiceOver users
    // Example: Show persistent overlays instead of auto-dismissing
}
```

### VoiceOver Notifications

**Listen for VoiceOver status changes:**
- Observe `voiceOverStatusDidChangeNotification`
- Update UI when VoiceOver starts/stops

```swift
NotificationCenter.default.addObserver(
    self,
    selector: #selector(voiceOverStatusChanged),
    name: UIAccessibility.voiceOverStatusDidChangeNotification,
    object: nil
)

@objc private func voiceOverStatusChanged() {
    if UIAccessibility.isVoiceOverRunning {
        // Adjust UI for VoiceOver
    }
}
```

## Required Properties

### Minimum Requirements

**Every accessible element must have:**
1. `isAccessibilityElement = true` (for custom views)
2. `accessibilityLabel` (identifies element)
3. Appropriate `accessibilityTraits` (describes type)

**Optional but recommended:**
- `accessibilityHint` (when action isn't obvious)
- `accessibilityValue` (for dynamic content)

### Property Order

**Set properties in this order:**
1. `isAccessibilityElement`
2. `accessibilityLabel`
3. `accessibilityTraits`
4. `accessibilityValue` (if needed)
5. `accessibilityHint` (if needed)

```swift
// ✅ Correct order
element.isAccessibilityElement = true
element.accessibilityLabel = "Element name"
element.accessibilityTraits = .button
element.accessibilityValue = "Current value"
element.accessibilityHint = "Action description"
```

## Best Practices

### Label Writing

**Guidelines:**
- Use nouns for static elements ("Save button")
- Use verbs for actions only in hints
- Avoid redundant words ("button" already implied by trait)
- Match visible text when possible

```swift
// ✅ Good: Concise, clear
button.accessibilityLabel = "Save"

// ✅ Good: Matches visible text
button.setTitle("Delete", for: .normal)
button.accessibilityLabel = "Delete"

// ❌ Bad: Redundant
button.accessibilityLabel = "Save button" // "button" redundant
```

### Dynamic Labels

**Update labels when content changes:**
- Refresh accessibility properties after state changes
- Use `UIAccessibility.post()` for important updates

```swift
func updateScore(_ newScore: Int) {
    scoreLabel.text = "\(newScore)"
    scoreLabel.accessibilityLabel = "Score"
    scoreLabel.accessibilityValue = "\(newScore) points"
    
    // Announce important changes
    UIAccessibility.post(notification: .announcement, argument: "Score updated to \(newScore)")
}
```

### Container Accessibility

**Grouping related elements:**
- Use `accessibilityContainer` for complex hierarchies
- Set container's `isAccessibilityElement = false`
- Let child elements handle their own accessibility

```swift
// Complex view hierarchy
parentView.isAccessibilityElement = false
childView1.isAccessibilityElement = true
childView1.accessibilityLabel = "First item"
childView2.isAccessibilityElement = true
childView2.accessibilityLabel = "Second item"
```

### Localization

**All accessibility strings must be localized:**
- Use `NSLocalizedString()` for labels and hints
- Test with different languages
- Ensure VoiceOver announcements work in all supported languages

```swift
button.accessibilityLabel = NSLocalizedString("save_button_label", comment: "Save button")
button.accessibilityHint = NSLocalizedString("save_button_hint", comment: "Saves the current document")
```

## Unreachable Elements Detection

The most common UIKit a11y bug: a container view with `isAccessibilityElement = true` absorbs all VoiceOver focus — its children become completely unreachable. This causes ~44% of real VoiceOver violations.

**Symptoms:**
- VoiceOver reads a container as a single element ("Navigation content", "Letter tile")
- Individual buttons inside cannot be reached by swiping
- Elements are visually present but VoiceOver skips them entirely

**Fix pattern:**

```swift
// ❌ Wrong: container swallows all 5 buttons inside
bottomBarView.isAccessibilityElement = true  // reads as "Navigation content" only

// ✅ Fix 1: disable container, list children explicitly
bottomBarView.isAccessibilityElement = false
bottomBarView.accessibilityElements = [backBtn, deleteBtn, saveBtn, shareBtn, moreBtn]

// ✅ Fix 2: ordering on the parent view
view.accessibilityElements = [headerLabel, contentArea, actionButton, dismissButton]

// ✅ Fix 3: individual elements self-declare (when container has no isAccessibilityElement set)
containerView.isAccessibilityElement = false  // default; children are visible to VoiceOver
```

**Detection checklist:**
- Container has `isAccessibilityElement = true` AND contains interactive children → likely bug
- VoiceOver reads a container label but its buttons are unreachable → container needs `= false`
- Three-dot menus, bottom bars, nav bars are common culprits

---

## Dynamic Type (UIKit)

Dynamic Type lets users choose their preferred text size in Settings. Apps must honor this setting.

```swift
// ✅ Use text styles — automatically scales with user's preference
label.font = UIFont.preferredFont(forTextStyle: .body)
label.adjustsFontForContentSizeCategory = true  // Required for live updates!

// ✅ React to size changes (e.g., after user returns from Settings)
override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
    super.traitCollectionDidChange(previousTraitCollection)
    if traitCollection.preferredContentSizeCategory != previousTraitCollection?.preferredContentSizeCategory {
        label.font = UIFont.preferredFont(forTextStyle: .body)
        titleLabel.font = UIFont.preferredFont(forTextStyle: .title1)
    }
}

// ✅ Custom font that scales
let customFont = UIFont(name: "MyFont-Regular", size: 16)!
label.font = UIFontMetrics(forTextStyle: .body).scaledFont(for: customFont)
label.adjustsFontForContentSizeCategory = true

// ❌ Anti-pattern: fixed sizes don't scale
label.font = UIFont.systemFont(ofSize: 16)        // doesn't scale
label.font = UIFont(name: "CustomFont", size: 14) // doesn't scale without UIFontMetrics
```

**UIFont text styles:** `.largeTitle`, `.title1/.title2/.title3`, `.headline`, `.body`, `.callout`, `.subheadline`, `.footnote`, `.caption1/.caption2`

Test: Go to **Settings → Accessibility → Display & Text Size → Larger Text** and drag to max. All text should enlarge.

---

## SwiftUI Accessibility Modifiers (iOS 15+)

Modern SwiftUI modifiers for accessibility — project targets iOS 16+, no availability guards needed for iOS 15+ APIs.

| Modifier | Purpose |
|----------|---------|
| `.accessibilityLabel(_:isEnabled:)` | Conditional label — apply label only when `isEnabled` is true |
| `.accessibilityAddTraits(.isHeader)` | Add `.isHeader`, `.isButton`, `.isToggle`, etc. to element |
| `.accessibilityElement(children: .ignore)` | Combine/contain/ignore child elements (`.combine`, `.contain`, `.ignore`) |
| `.accessibilityFocused($binding)` | Programmatic VoiceOver focus management via `@AccessibilityFocusState` |
| `.accessibilityAdjustableAction { action in }` | Handle `.increment`/`.decrement` for adjustable elements |

```swift
// ✅ Conditional label
Text(item.name)
    .accessibilityLabel(item.name, isEnabled: !item.name.isEmpty)

// ✅ Traits
Text("Section Title")
    .accessibilityAddTraits(.isHeader)

// ✅ Group children
HStack {
    Image(systemName: "heart.fill")
    Text("Liked")
}
.accessibilityElement(children: .combine)

// ✅ Adjustable action
Slider(value: $volume)
    .accessibilityAdjustableAction { action in
        switch action {
        case .increment: volume = min(1, volume + 0.1)
        case .decrement: volume = max(0, volume - 0.1)
        default: break
        }
    }
```

## Dynamic Type & Reduce Motion

**Respond to user accessibility preferences:**

```swift
// Dynamic Type — scale a custom dimension with the user's text size preference
@ScaledMetric var iconSize: CGFloat = 24

// Reduce motion — skip animations when the user prefers reduced motion
@Environment(\.accessibilityReduceMotion) var reduceMotion

var body: some View {
    Circle()
        .frame(width: iconSize, height: iconSize)
        .animation(reduceMotion ? nil : .spring(), value: isExpanded)
}

// Observe current Dynamic Type category
@Environment(\.dynamicTypeSize) var typeSize

// UIKit equivalent (read current preferred category)
let category = UIApplication.shared.preferredContentSizeCategory
```

**Rules:**
- Never hard-code font sizes for body text — use `.font(.body)` / `.font(.headline)` etc.
- Use `@ScaledMetric` for spacing/icon sizes that should scale with text size
- Always gate animations behind `accessibilityReduceMotion`
- Test with largest Accessibility text size in Settings

> **Note:** For UIKit Dynamic Type patterns see [Dynamic Type (UIKit)](#dynamic-type-uikit) above.

## Complex Image Accessibility

### Charts and Graphs
Describe data insights, not visual appearance. Include key numbers/percentages and trends.

```swift
chartView.accessibilityLabel = "Sales chart"
chartView.accessibilityValue = "Sales increased from $10,000 to $15,000 over 6 months, showing 50% growth"
```

### Diagrams and Flowcharts
Describe structure, relationships, and key steps.

```swift
flowDiagram.accessibilityLabel = "User registration flow diagram"
flowDiagram.accessibilityValue = "Shows steps: enter email, verify email, create password, complete profile"
```

### Maps
Identify what the map shows, include location names, describe relevant features.

```swift
mapView.accessibilityLabel = "Map of downtown area"
mapView.accessibilityValue = "Shows user's current location at Main Street and 5th Avenue, with nearby restaurants highlighted"
```
