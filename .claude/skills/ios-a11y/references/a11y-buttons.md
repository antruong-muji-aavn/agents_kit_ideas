---
name: a11y-buttons
description: Button accessibility rules — labels, traits, states, icon buttons, toggle buttons, groups
---

# Button Accessibility Rules

## Purpose

Accessibility rules specific to buttons and button-like interactive elements, ensuring VoiceOver users can identify and interact with all buttons.

## Table of Contents

- [Button Basics](#button-basics)
- [Button Labels](#button-labels)
- [Button States](#button-states)
- [Toggle Buttons](#toggle-buttons)
- [Icon Buttons](#icon-buttons)
- [Button Groups](#button-groups)
- [Custom Buttons](#custom-buttons)
- [Buttons Read as Images](#buttons-read-as-images)
- [Navigation Bar / Bottom Bar Button Groups](#navigation-bar--bottom-bar-button-groups)
- [Tab Bar Item Accessibility](#tab-bar-item-accessibility)

## Related Documents

- [a11y-core](./a11y-core.md) - Core accessibility principles
- [a11y-forms](./a11y-forms.md) - Form accessibility
- [a11y-focus](./a11y-focus.md) - Focus management

## Button Basics

### Standard UIButton

**Default Behavior:**
- `UIButton` is accessible by default
- Title text becomes accessibility label automatically
- `.button` trait is set automatically
- No additional setup needed for simple buttons

```swift
// ✅ Standard button - automatically accessible
let button = UIButton(type: .system)
button.setTitle("Save", for: .normal)
// VoiceOver reads: "Save, Button"
```

### Button Trait

**Always set `.button` trait:**
- Required for VoiceOver to recognize as button
- Enables button-specific gestures
- Standard buttons have this automatically

```swift
// ✅ Explicit trait setting
customButton.accessibilityTraits = .button

// ✅ Multiple traits
customButton.accessibilityTraits = [.button, .selected]
```

## Button Labels

### Label Guidelines

**Rules:**
- Use verb or action word ("Save", "Delete", "Cancel")
- Match visible button text when possible
- Keep concise (1-2 words preferred)
- No trailing punctuation

```swift
// ✅ Good labels
button.accessibilityLabel = "Save"
button.accessibilityLabel = "Delete item"
button.accessibilityLabel = "Cancel"

// ❌ Bad labels
button.accessibilityLabel = "Save button" // Redundant
button.accessibilityLabel = "Click here to save" // Too verbose
button.accessibilityLabel = "Save!" // Punctuation unnecessary
```

### Buttons with Icons Only

**Icon-only buttons must have labels:**
- Never rely on icon alone
- Provide descriptive label
- Consider hint if action isn't obvious

```swift
// ✅ Icon button with label
let closeButton = UIButton(type: .system)
closeButton.setImage(UIImage(systemName: "xmark"), for: .normal)
closeButton.accessibilityLabel = "Close"
closeButton.accessibilityTraits = .button

// ✅ Icon button with descriptive label
let shareButton = UIButton(type: .system)
shareButton.setImage(UIImage(systemName: "square.and.arrow.up"), for: .normal)
shareButton.accessibilityLabel = "Share"
shareButton.accessibilityHint = "Opens share options"
```

### Buttons with Both Text and Icon

**Prefer text as label:**
- Use visible text as accessibility label
- Icon is decorative in this case
- No need to mention icon in label

```swift
// ✅ Button with text and icon
let saveButton = UIButton(type: .system)
saveButton.setTitle("Save", for: .normal)
saveButton.setImage(UIImage(systemName: "checkmark"), for: .normal)
saveButton.accessibilityLabel = "Save" // Text is sufficient

// ❌ Don't mention icon
saveButton.accessibilityLabel = "Save with checkmark icon" // Unnecessary
```

## Button States

### Enabled/Disabled

**Disabled buttons:**
- Set `.notEnabled` trait
- Update label to indicate disabled state
- VoiceOver announces "dimmed" automatically

```swift
// ✅ Disabled button
saveButton.isEnabled = false
saveButton.accessibilityTraits = [.button, .notEnabled]
// VoiceOver reads: "Save, Button, Dimmed"
```

### Selected State

**Selected buttons:**
- Add `.selected` trait
- Update value if state is meaningful
- Common for segmented controls, filters

```swift
// ✅ Selected button
filterButton.isSelected = true
filterButton.accessibilityTraits = [.button, .selected]
filterButton.accessibilityValue = "Selected"

// ✅ Toggle button with state
toggleButton.accessibilityLabel = "Dark mode"
toggleButton.accessibilityValue = isEnabled ? "On" : "Off"
toggleButton.accessibilityTraits = [.button, .toggleButton]
```

### Loading State

**Buttons in loading state:**
- Update label to indicate loading
- Consider disabling interaction
- Announce state changes

```swift
// ✅ Loading button
func setLoading(_ isLoading: Bool) {
    if isLoading {
        button.accessibilityLabel = "Loading"
        button.isEnabled = false
        button.accessibilityTraits = [.button, .notEnabled]
    } else {
        button.accessibilityLabel = "Submit"
        button.isEnabled = true
        button.accessibilityTraits = .button
    }
}
```

## Toggle Buttons

### Toggle Button Trait

**Toggle buttons:**
- Use `.toggleButton` trait (available since iOS 13; project targets iOS 18+)
- Set value to "On" or "Off"
- VoiceOver announces state clearly

```swift
// ✅ Toggle button
darkModeButton.accessibilityTraits = [.button, .toggleButton]
darkModeButton.accessibilityValue = isDarkMode ? "On" : "Off"
```

### Toggle Button Labels

**Labels for toggles:**
- Describe what is being toggled
- Value indicates current state
- Hint explains what happens when toggled

```swift
// ✅ Toggle button setup
muteButton.accessibilityLabel = "Sound"
muteButton.accessibilityValue = isMuted ? "Muted" : "Unmuted"
muteButton.accessibilityHint = "Double tap to toggle sound"
muteButton.accessibilityTraits = [.button, .toggleButton]
```

## Icon Buttons

### Decorative Icons

**Icons without text:**
- Always provide accessibility label
- Describe action, not icon appearance
- Use hint if action needs clarification

```swift
// ✅ Icon button labels
closeButton.accessibilityLabel = "Close"
editButton.accessibilityLabel = "Edit"
deleteButton.accessibilityLabel = "Delete"
shareButton.accessibilityLabel = "Share"

// ✅ With hints for clarity
infoButton.accessibilityLabel = "Information"
infoButton.accessibilityHint = "Opens help documentation"
```

### Common Icon Labels

**Standard icon mappings:** ✕/× → "Close", ✓ → "Done"/"Confirm", ⚙ → "Settings", ℹ → "Information", ♥ → "Favorite"/"Like", 📤 → "Share", 🗑 → "Delete", ✏ → "Edit"

## Button Groups

### Segmented Controls

**UISegmentedControl:**
- Accessible by default
- Each segment is announced separately
- Value indicates selected segment

```swift
// ✅ Segmented control (accessible by default)
let segmentedControl = UISegmentedControl(items: ["List", "Grid"])
segmentedControl.selectedSegmentIndex = 0
// VoiceOver reads: "List, Selected, Segmented control"
```

### Button Groups

**Grouped buttons:** Each button is separate accessibility element. Use container with `isAccessibilityElement = false` to group. Consider hint to indicate group context.

## Custom Buttons

### Custom Button Classes

**Custom button implementations:**
- Must set `isAccessibilityElement = true`
- Must set `.button` trait
- Must provide label

```swift
// ✅ Custom button class
class CustomButton: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupAccessibility()
    }
    
    private func setupAccessibility() {
        isAccessibilityElement = true
        accessibilityTraits = .button
        accessibilityLabel = "Custom action"
    }
    
    func setTitle(_ title: String) {
        accessibilityLabel = title
    }
}
```

### Action Buttons

**Buttons that perform actions:** Label describes action, hint explains result, update label if action changes.

## Buttons Read as Images

**Problem:** Buttons that display only an image (no title text) are announced as "image" by VoiceOver — making them appear non-interactive. Accounts for ~44% of real VoiceOver violations in production apps.

**Detection:** Button uses `setImage()` with no `setTitle()`, or a `UIView` with `UIImageView` + tap gesture but no explicit traits.

```swift
// ❌ Wrong: VoiceOver reads "image" — not interactive
let orderBtn = UIButton()
orderBtn.setImage(UIImage(named: "jetzt_bestellen"), for: .normal)
// Missing: accessibilityLabel, wrong default trait

// ✅ Fix: explicit label + button trait
orderBtn.isAccessibilityElement = true
orderBtn.accessibilityTraits = .button          // NOT .image
orderBtn.accessibilityLabel = "Order now"       // Describe the ACTION, not the image

// ✅ Custom UIView acting as button (e.g. letter tile):
class LetterTileView: UIView {
    var senderName: String = "" {
        didSet { accessibilityLabel = "Letter from \(senderName)" }
    }
    override init(frame: CGRect) {
        super.init(frame: frame)
        isAccessibilityElement = true
        accessibilityTraits = .button
    }
    required init?(coder: NSCoder) { super.init(coder: coder) }
}
```

---

## Navigation Bar / Bottom Bar Button Groups

**Problem:** Bottom navigation bars and toolbars with `isAccessibilityElement = true` on the container are read as a single element ("Navigation content") — all individual buttons inside become unreachable.

```swift
// ❌ Wrong: VoiceOver reads "Navigation content" only — 5 buttons invisible
letterDetailBottomBar.isAccessibilityElement = true

// ✅ Fix: disable container, enumerate children explicitly
letterDetailBottomBar.isAccessibilityElement = false
letterDetailBottomBar.accessibilityElements = [
    backButton,
    deleteButton,
    saveButton,
    shareButton,
    moreOptionsButton
]

// Each button must have its own label and .button trait:
backButton.accessibilityLabel = "Back"
backButton.accessibilityTraits = .button

deleteButton.accessibilityLabel = "Delete letter"
deleteButton.accessibilityTraits = .button

moreOptionsButton.accessibilityLabel = "More options"
moreOptionsButton.accessibilityTraits = .button
```

---

## Tab Bar Item Accessibility

**Problem:** Tab bar items read as icon file names (e.g., "epost ic 40x40") instead of human-readable labels. Only the active tab may be reachable via swipe.

```swift
// ❌ Wrong: VoiceOver reads "epost ic 40x40" — useless to user
let ePostItem = UITabBarItem()
ePostItem.image = UIImage(named: "epost_ic_40x40")

// ✅ Fix: always pass title to UITabBarItem
let ePostItem = UITabBarItem(
    title: "ePost",
    image: UIImage(named: "epost_ic_40x40"),
    selectedImage: UIImage(named: "epost_ic_40x40_selected")
)

// ✅ For custom tab bar views — set per-button labels and states
func updateTabAccessibility() {
    for (i, tabButton) in tabButtons.enumerated() {
        tabButton.isAccessibilityElement = true
        tabButton.accessibilityLabel = tabTitles[i]           // e.g., "ePost"
        tabButton.accessibilityTraits = (i == selectedIndex)
            ? [.button, .selected]
            : .button
    }
}

// ✅ Custom tab bar container must NOT block children
customTabBar.isAccessibilityElement = false

// ✅ Announce tab change to VoiceOver
func selectTab(_ index: Int) {
    selectedIndex = index
    updateTabAccessibility()
    if UIAccessibility.isVoiceOverRunning {
        UIAccessibility.post(notification: .screenChanged, argument: tabButtons[index])
    }
}
```

---

### Best Practices Summary

**Button Accessibility Checklist:**
- ✅ Button has `.button` trait
- ✅ Label is concise and action-oriented
- ✅ Icon-only buttons have descriptive labels
- ✅ State changes are reflected in traits/value
- ✅ Disabled buttons have `.notEnabled` trait
- ✅ Toggle buttons use `.toggleButton` trait
- ✅ Hints provided when action isn't obvious
- ✅ All labels are localized
