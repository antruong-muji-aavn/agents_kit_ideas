---
name: a11y-colors-contrast
description: Color and contrast accessibility — WCAG contrast ratios, color independence, visual indicators
---

# Color and Contrast Accessibility Rules

## Purpose

Accessibility rules for color usage and contrast ratios, ensuring content is perceivable by users with color vision deficiencies and low vision, meeting WCAG 2.1 AA standards.

## Table of Contents

- [Contrast Requirements](#contrast-requirements)
- [Color Independence](#color-independence)
- [Text Contrast](#text-contrast)
- [Interactive Elements](#interactive-elements)
- [Visual Indicators](#visual-indicators)
- [Testing](#testing)

## Related Documents

- [a11y-core](./a11y-core.md) - Core accessibility principles
- [a11y-buttons](./a11y-buttons.md) - Button accessibility
- [a11y-forms](./a11y-forms.md) - Form accessibility
- [a11y-testing](./a11y-testing.md) - Testing guidelines

## Contrast Requirements

### WCAG 2.1 AA Standards

**Contrast ratio requirements:**
- **Normal text** (under 18pt): 4.5:1 minimum
- **Large text** (18pt+ or 14pt+ bold): 3:1 minimum
- **UI components**: 3:1 minimum for visual information
- **Graphics**: 3:1 minimum for essential information

```swift
// ✅ Contrast calculation helper
extension UIColor {
    func contrastRatio(with color: UIColor) -> CGFloat {
        let luminance1 = self.luminance
        let luminance2 = color.luminance
        let lighter = max(luminance1, luminance2)
        let darker = min(luminance1, luminance2)
        return (lighter + 0.05) / (darker + 0.05)
    }
    
    var luminance: CGFloat {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        self.getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        
        let r = red <= 0.03928 ? red / 12.92 : pow((red + 0.055) / 1.055, 2.4)
        let g = green <= 0.03928 ? green / 12.92 : pow((green + 0.055) / 1.055, 2.4)
        let b = blue <= 0.03928 ? blue / 12.92 : pow((blue + 0.055) / 1.055, 2.4)
        
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
}

// Usage
let textColor = UIColor.label
let backgroundColor = UIColor.systemBackground
let ratio = textColor.contrastRatio(with: backgroundColor)
// Ensure ratio >= 4.5 for normal text
```

### Text Contrast Examples

**Meeting contrast requirements:**
- Use system colors that adapt to light/dark mode
- Test contrast in both light and dark modes
- Verify custom colors meet ratios

```swift
// ✅ Good contrast (system colors)
let label = UILabel()
label.textColor = .label // Adapts to mode, meets contrast
label.backgroundColor = .systemBackground

// ✅ Custom colors with good contrast
let customLabel = UILabel()
customLabel.textColor = UIColor(white: 0.1, alpha: 1.0) // Dark text
customLabel.backgroundColor = UIColor(white: 0.95, alpha: 1.0) // Light background
// Ratio: ~18:1 (exceeds requirement)

// ❌ Poor contrast
let badLabel = UILabel()
badLabel.textColor = UIColor(white: 0.5, alpha: 1.0) // Medium gray
badLabel.backgroundColor = UIColor(white: 0.6, alpha: 1.0) // Similar gray
// Ratio: ~1.1:1 (fails requirement)
```

## Color Independence

### Don't Rely on Color Alone

**WCAG requirement:**
- Color cannot be the only means of conveying information
- Use additional indicators: icons, text, patterns, shapes
- Ensure information is accessible without color perception

```swift
// ✅ Good: Color + icon
let statusView = UIView()
let statusIcon = UIImageView()
let statusLabel = UILabel()

if isOnline {
    statusView.backgroundColor = .systemGreen
    statusIcon.image = UIImage(systemName: "checkmark.circle.fill")
    statusLabel.text = "Online"
    statusLabel.accessibilityLabel = "Status: Online"
} else {
    statusView.backgroundColor = .systemRed
    statusIcon.image = UIImage(systemName: "xmark.circle.fill")
    statusLabel.text = "Offline"
    statusLabel.accessibilityLabel = "Status: Offline"
}

// ❌ Bad: Color only
if isOnline {
    statusView.backgroundColor = .systemGreen // Only color indicator
} else {
    statusView.backgroundColor = .systemRed // Only color indicator
}
```

### Error Indicators

**Multiple indicators for errors:**
- Use color + icon + text
- Provide accessible error messages
- Don't rely on red color alone

```swift
// ✅ Error with multiple indicators
func showError(_ message: String, for field: UITextField) {
    // Visual: Red border
    field.layer.borderColor = UIColor.systemRed.cgColor
    field.layer.borderWidth = 2
    
    // Visual: Error icon
    let errorIcon = UIImageView(image: UIImage(systemName: "exclamationmark.triangle.fill"))
    errorIcon.tintColor = .systemRed
    
    // Text: Error message
    let errorLabel = UILabel()
    errorLabel.text = message
    errorLabel.textColor = .systemRed
    
    // Accessibility: Announce error
    field.accessibilityValue = message
    UIAccessibility.post(notification: .announcement, argument: message)
}

// ❌ Bad: Color only
func showErrorBad(for field: UITextField) {
    field.textColor = .systemRed // Only color, no other indicator
}
```

## Text Contrast

### Normal Text

**4.5:1 minimum contrast:**
- Applies to text under 18pt regular weight
- Applies to text under 14pt bold weight
- Most body text falls in this category

```swift
// ✅ Normal text with good contrast
let bodyLabel = UILabel()
bodyLabel.font = UIFont.systemFont(ofSize: 16) // Normal text
bodyLabel.textColor = .label // High contrast
bodyLabel.backgroundColor = .systemBackground

// Verify contrast ratio >= 4.5:1
let ratio = bodyLabel.textColor!.contrastRatio(with: bodyLabel.backgroundColor!)
assert(ratio >= 4.5, "Contrast ratio must be at least 4.5:1")
```

### Large Text

**3:1 minimum contrast:**
- Applies to text 18pt+ regular weight
- Applies to text 14pt+ bold weight
- Headings often qualify as large text

```swift
// ✅ Large text
let headingLabel = UILabel()
headingLabel.font = UIFont.boldSystemFont(ofSize: 24) // Large text
headingLabel.textColor = .label
headingLabel.backgroundColor = .systemBackground
// 3:1 minimum required (system colors meet this)

// ✅ Bold large text
let boldLabel = UILabel()
boldLabel.font = UIFont.boldSystemFont(ofSize: 16) // 14pt+ bold
boldLabel.textColor = .label
// 3:1 minimum required
```

## Interactive Elements

### Button Contrast

**Interactive elements need clear contrast:**
- Buttons must be distinguishable from background
- Focus states must be visible
- Disabled states must be clear

```swift
// ✅ Button with good contrast
let button = UIButton(type: .system)
button.setTitleColor(.white, for: .normal)
button.backgroundColor = .systemBlue
// Ensure contrast between text and background >= 4.5:1

// ✅ Focus state
button.layer.borderWidth = 2
button.layer.borderColor = UIColor.systemBlue.cgColor
// Focus indicator visible

// ✅ Disabled state
button.setTitleColor(.systemGray, for: .disabled)
button.backgroundColor = .systemGray5
// Disabled state clearly distinguishable
```

### Link Contrast

**Links need sufficient contrast:**
- Links must meet text contrast requirements
- Underline helps but doesn't replace contrast
- Visited links must also meet contrast

```swift
// ✅ Link with good contrast
let linkLabel = UILabel()
linkLabel.text = "Learn more"
linkLabel.textColor = .systemBlue // High contrast blue
linkLabel.accessibilityTraits = .link

// Ensure link color contrasts with background
let ratio = linkLabel.textColor!.contrastRatio(with: .systemBackground)
// Must be >= 4.5:1 for normal text
```

## Visual Indicators

### Focus Indicators

**Focus must be visually apparent:** Use high-contrast borders or outlines, don't rely solely on color change, ensure 3:1 contrast for focus indicators.

### Status Indicators

**Status must be clear without color:** Use icons, shapes, or patterns. Provide text labels. Ensure sufficient contrast. Use multiple indicators: color + icon + text.

## Testing

### Contrast Testing Tools

**Verify contrast ratios:** Use online contrast checkers, use Xcode's accessibility inspector, test in both light and dark modes, verify custom color combinations.

### Color Blindness Testing

**Test with color vision simulators:** Use tools to simulate color blindness, verify information is accessible, test with grayscale mode, ensure non-color indicators work.

### Dark Mode Testing

**Test in both modes:** Verify contrast in light mode, verify contrast in dark mode, ensure all indicators work in both modes, test system color adaptations.

### Best Practices Summary

**Color and Contrast Checklist:**
- ✅ All text meets contrast requirements (4.5:1 normal, 3:1 large)
- ✅ Interactive elements have sufficient contrast
- ✅ Color is not the only indicator of information
- ✅ Icons, text, or shapes supplement color
- ✅ Focus indicators are visible and high contrast
- ✅ Error states use multiple indicators
- ✅ Status indicators don't rely on color alone
- ✅ Tested in both light and dark modes
- ✅ Tested with color blindness simulators
- ✅ All visual information is accessible
