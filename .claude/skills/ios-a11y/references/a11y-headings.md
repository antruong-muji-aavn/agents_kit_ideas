---
name: a11y-headings
description: Heading structure accessibility — .header trait, heading levels, navigation, dynamic content
---

# Heading Accessibility Rules

## Purpose

Accessibility rules for heading structure and navigation, ensuring VoiceOver users can efficiently navigate content using heading navigation.

## Table of Contents

- [Heading Structure](#heading-structure)
- [Heading Levels](#heading-levels)
- [Heading Labels](#heading-labels)
- [Navigation](#navigation)
- [Dynamic Headings](#dynamic-headings)
- [Best Practices](#best-practices)

## Related Documents

- [a11y-core](./a11y-core.md) - Core accessibility principles
- [a11y-forms](./a11y-forms.md) - Form accessibility
- [a11y-focus](./a11y-focus.md) - Focus management

## Heading Structure

### Heading Trait

**Use `.header` trait:**
- Identifies element as heading to VoiceOver
- Enables heading navigation mode
- Required for heading navigation

```swift
// ✅ Heading with trait
let titleLabel = UILabel()
titleLabel.text = "Settings"
titleLabel.accessibilityTraits = .header
// VoiceOver reads: "Settings, Heading"

// ✅ Multiple traits
let sectionTitle = UILabel()
sectionTitle.text = "Account"
sectionTitle.accessibilityTraits = [.header, .button] // If clickable
```

### Heading Hierarchy

**Maintain logical hierarchy:**
- Use H1 for main page title
- Use H2 for major sections
- Use H3 for subsections
- Don't skip levels (H1 → H3 is wrong)

```swift
// ✅ Proper hierarchy
let pageTitle = UILabel()
pageTitle.text = "User Profile" // H1
pageTitle.accessibilityTraits = .header
// Set heading level if needed (see below)

let sectionTitle = UILabel()
sectionTitle.text = "Personal Information" // H2
sectionTitle.accessibilityTraits = .header

let subsectionTitle = UILabel()
subsectionTitle.text = "Contact Details" // H3
subsectionTitle.accessibilityTraits = .header
```

## Heading Levels

### Setting Heading Levels

**iOS 13+ heading levels:**
- Use `accessibilityAttributedLabel` with heading level
- Or use `UIAccessibilityTraits` with custom heading level
- Levels 1-6 correspond to H1-H6

```swift
// ✅ Heading level (iOS 13+)
if #available(iOS 13.0, *) {
    let attributedLabel = NSMutableAttributedString(string: "Page Title")
    attributedLabel.setAttributes(
        [.accessibilityTextHeadingLevel: 1],
        range: NSRange(location: 0, length: attributedLabel.length)
    )
    titleLabel.accessibilityAttributedLabel = attributedLabel
}
```

### Heading Level Guidelines

**Level assignments:**
- **Level 1**: Main page/screen title (one per screen)
- **Level 2**: Major sections (Settings, Profile, etc.)
- **Level 3**: Subsections within major sections
- **Level 4-6**: Deeper nesting if needed

```swift
// ✅ Heading levels example
// Page title - Level 1
let pageTitle = UILabel()
pageTitle.text = "Settings"
pageTitle.accessibilityTraits = .header
// Set level 1

// Section - Level 2
let accountSection = UILabel()
accountSection.text = "Account"
accountSection.accessibilityTraits = .header
// Set level 2

// Subsection - Level 3
let profileSubsection = UILabel()
profileSubsection.text = "Profile Information"
profileSubsection.accessibilityTraits = .header
// Set level 3
```

### Programmatic Heading Levels

**Setting levels programmatically:**
- Use `NSAttributedString` with `accessibilityTextHeadingLevel`
- Or implement custom heading level logic
- Ensure consistent level assignment

```swift
// ✅ Helper function for heading levels
extension UILabel {
    func setHeadingLevel(_ level: Int) {
        guard #available(iOS 13.0, *) else { return }
        
        let attributedString = NSMutableAttributedString(string: self.text ?? "")
        attributedString.setAttributes(
            [.accessibilityTextHeadingLevel: level],
            range: NSRange(location: 0, length: attributedString.length)
        )
        self.accessibilityAttributedLabel = attributedString
        self.accessibilityTraits = .header
    }
}

// Usage
titleLabel.setHeadingLevel(1)
sectionLabel.setHeadingLevel(2)
```

## Heading Labels

### Label Guidelines

**Heading label rules:**
- Should be concise and descriptive
- Match visible text when possible
- Don't include "heading" in label
- Use sentence case

```swift
// ✅ Good heading labels
heading.accessibilityLabel = "Settings"
heading.accessibilityLabel = "Account Information"
heading.accessibilityLabel = "Privacy Settings"

// ❌ Bad heading labels
heading.accessibilityLabel = "Settings heading" // Redundant
heading.accessibilityLabel = "SETTINGS" // All caps unnecessary
heading.accessibilityLabel = "This is the settings section heading" // Too verbose
```

### Dynamic Headings

**Update headings when content changes:**
- Refresh accessibility properties
- Maintain heading structure
- Announce significant changes

```swift
// ✅ Dynamic heading update
func updateSectionTitle(_ newTitle: String) {
    sectionTitle.text = newTitle
    sectionTitle.accessibilityLabel = newTitle
    sectionTitle.accessibilityTraits = .header
    
    // Announce if significant
    if UIAccessibility.isVoiceOverRunning {
        UIAccessibility.post(
            notification: .announcement,
            argument: "Section updated: \(newTitle)"
        )
    }
}
```

## Navigation

### Heading Navigation

**VoiceOver heading navigation:**
- Users can swipe right/left to navigate headings
- Headings provide quick content overview
- Essential for long-form content

```swift
// ✅ Screen with heading navigation
class SettingsViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Main title - H1
        let titleLabel = UILabel()
        titleLabel.text = "Settings"
        titleLabel.accessibilityTraits = .header
        titleLabel.setHeadingLevel(1)
        
        // Sections - H2
        let accountSection = createSection(title: "Account", level: 2)
        let privacySection = createSection(title: "Privacy", level: 2)
        let aboutSection = createSection(title: "About", level: 2)
    }
    
    private func createSection(title: String, level: Int) -> UILabel {
        let label = UILabel()
        label.text = title
        label.accessibilityTraits = .header
        label.setHeadingLevel(level)
        return label
    }
}
```

### Skip Navigation

**Provide skip links:**
- Use headings to mark main content start
- Help users skip repetitive navigation
- Common pattern: Skip to main content

```swift
// ✅ Skip to main content
let skipButton = UIButton(type: .system)
skipButton.setTitle("Skip to main content", for: .normal)
skipButton.accessibilityLabel = "Skip to main content"
skipButton.accessibilityHint = "Skips navigation and goes to main content"

let mainContentHeading = UILabel()
mainContentHeading.text = "Main Content"
mainContentHeading.accessibilityTraits = .header
mainContentHeading.setHeadingLevel(1)
```

## Dynamic Headings

### Content Updates

**Update headings dynamically:**
- Refresh heading properties when content changes
- Maintain heading hierarchy
- Announce significant structural changes

```swift
// ✅ Dynamic heading updates
func reloadContent() {
    // Clear old headings
    oldHeadings.forEach { $0.isAccessibilityElement = false }
    
    // Create new headings
    let newHeadings = createHeadings()
    newHeadings.forEach { heading in
        heading.accessibilityTraits = .header
        heading.setHeadingLevel(2)
    }
    
    // Announce if needed
    if UIAccessibility.isVoiceOverRunning {
        UIAccessibility.post(
            notification: .screenChanged,
            argument: nil
        )
    }
}
```

### Conditional Headings

**Show/hide headings:** Set `isAccessibilityElement = false` when hidden, restore when shown, maintain hierarchy when toggling.

## Best Practices

### Heading Checklist

**Every screen should have:** At least one H1 (main title), logical heading hierarchy, headings marked with `.header` trait, heading levels set appropriately, headings match visible text.

### Common Patterns

**Settings:** H1 for screen title, H2 for sections. **Forms:** H1 for form title, H2 for form sections.

### Anti-patterns

**Don't:** Use headings for non-heading content, skip heading levels (H1 → H3), use too many H1 headings, make everything a heading, use headings for decorative text.

### Best Practices Summary

**Heading Rules:** Use `.header` trait for all headings, set appropriate heading levels (1-6), maintain logical hierarchy, one H1 per screen, headings match visible text, update headings when content changes, test with VoiceOver heading navigation, all heading text is localized.