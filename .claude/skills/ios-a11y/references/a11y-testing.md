---
name: a11y-testing
description: Accessibility testing — Xcode Inspector, VoiceOver testing, automated testing, checklists
---

# Accessibility Testing Rules

## Purpose

Testing guidelines and procedures for verifying iOS accessibility implementation, ensuring VoiceOver compatibility and WCAG 2.1 AA compliance.

## Table of Contents

- [Testing Tools](#testing-tools)
- [VoiceOver Testing](#voiceover-testing)
- [Automated Testing](#automated-testing)
- [Manual Testing Checklist](#manual-testing-checklist)
- [Testing Scenarios](#testing-scenarios)
- [Reporting Issues](#reporting-issues)

## Related Documents

- [a11y-core](./a11y-core.md) - Core accessibility principles
- [a11y-buttons](./a11y-buttons.md) - Button accessibility
- [a11y-forms](./a11y-forms.md) - Form accessibility
- [a11y-colors-contrast](./a11y-colors-contrast.md) - Color testing

## Testing Tools

### Xcode Accessibility Inspector

**Built-in testing tool:**
- Launch from Xcode: Product → Accessibility Inspector
- Inspect element properties
- Verify labels, hints, traits, values
- Check contrast ratios

```swift
// ✅ Test accessibility properties programmatically
func testAccessibilityProperties() {
    XCTAssertTrue(button.isAccessibilityElement, "Button should be accessible")
    XCTAssertNotNil(button.accessibilityLabel, "Button should have label")
    XCTAssertEqual(button.accessibilityTraits, .button, "Button should have button trait")
}
```

### VoiceOver Testing

**Enable VoiceOver:**
- Settings → Accessibility → VoiceOver → On
- Or Settings → Accessibility → VoiceOver → VoiceOver → On
- Triple-click home/side button shortcut
- Test all interactions with VoiceOver enabled

### Simulator Testing

**iOS Simulator:**
- Enable VoiceOver: Settings → Accessibility → VoiceOver
- Use Accessibility Inspector
- Test with different device sizes
- Test in light and dark modes

## VoiceOver Testing

### Basic Navigation

**Test VoiceOver navigation:**
- Swipe right: Next element
- Swipe left: Previous element
- Double tap: Activate element
- Swipe up/down: Change rotor settings
- Two-finger double tap: Start/stop reading

**Test checklist:**
- ✅ All interactive elements are reachable
- ✅ Navigation order is logical
- ✅ Headings are navigable
- ✅ Forms are completable
- ✅ Buttons are activatable

### Label Testing

**Verify labels are appropriate:**
- Listen to what VoiceOver reads
- Ensure labels are concise
- Verify labels match context
- Check for redundant information

```swift
// ✅ Test label quality
func testButtonLabels() {
    // Verify label exists
    XCTAssertNotNil(button.accessibilityLabel, "Button needs label")
    
    // Verify label is not too long
    if let label = button.accessibilityLabel {
        XCTAssertLessThan(label.count, 50, "Label should be concise")
    }
    
    // Verify label doesn't include redundant words
    XCTAssertFalse(
        button.accessibilityLabel?.contains("button") ?? false,
        "Label shouldn't include 'button'"
    )
}
```

### Trait Testing

**Verify correct traits:**
- Buttons have `.button` trait
- Headings have `.header` trait
- Links have `.link` trait
- Images have `.image` trait
- Disabled elements have `.notEnabled` trait

```swift
// ✅ Test traits
func testButtonTraits() {
    XCTAssertTrue(
        button.accessibilityTraits.contains(.button),
        "Button should have button trait"
    )
    
    if !button.isEnabled {
        XCTAssertTrue(
            button.accessibilityTraits.contains(.notEnabled),
            "Disabled button should have notEnabled trait"
        )
    }
}
```

### Value Testing

**Verify dynamic values:**
- Sliders announce current value
- Progress bars announce progress
- Toggles announce state
- Values update when state changes

```swift
// ✅ Test dynamic values
func testSliderValue() {
    slider.value = 0.5
    XCTAssertEqual(
        slider.accessibilityValue,
        "50 percent",
        "Slider should announce percentage"
    )
}

func testToggleValue() {
    toggle.isOn = true
    XCTAssertEqual(
        toggle.accessibilityValue,
        "On",
        "Toggle should announce state"
    )
}
```

## Automated Testing

### Unit Tests

**Test accessibility properties:**
- Verify labels are set
- Verify traits are correct
- Verify hints when needed
- Test dynamic updates

```swift
// ✅ Accessibility unit tests
class AccessibilityTests: XCTestCase {
    func testButtonAccessibility() {
        let button = UIButton(type: .system)
        button.setTitle("Save", for: .normal)
        
        XCTAssertTrue(button.isAccessibilityElement || button.titleLabel != nil)
        XCTAssertNotNil(button.accessibilityLabel)
        XCTAssertTrue(button.accessibilityTraits.contains(.button))
    }
    
    func testTextFieldAccessibility() {
        let textField = UITextField()
        textField.placeholder = "Email"
        textField.accessibilityLabel = "Email address"
        
        XCTAssertTrue(textField.isAccessibilityElement)
        XCTAssertEqual(textField.accessibilityLabel, "Email address")
    }
    
    func testImageViewAccessibility() {
        let imageView = UIImageView()
        imageView.image = UIImage(named: "profile")
        imageView.accessibilityLabel = "Profile photo"
        imageView.accessibilityTraits = .image
        
        XCTAssertTrue(imageView.isAccessibilityElement)
        XCTAssertEqual(imageView.accessibilityLabel, "Profile photo")
        XCTAssertTrue(imageView.accessibilityTraits.contains(.image))
    }
}
```

### UI Tests

**Test VoiceOver navigation:** Navigate through screens, verify focus order, test form completion, verify announcements.

## Manual Testing Checklist

### Screen-Level Testing

**For each screen:**
- ✅ Screen has descriptive title (H1)
- ✅ All interactive elements are accessible
- ✅ Focus order is logical
- ✅ Headings enable navigation
- ✅ Forms are completable
- ✅ Errors are announced
- ✅ Loading states are announced

### Element-Level Testing

**For each interactive element:**
- ✅ Has appropriate label
- ✅ Has correct traits
- ✅ Has hint if needed
- ✅ Has value if dynamic
- ✅ Is focusable
- ✅ Is activatable
- ✅ State changes are announced

### Form Testing

**For each form:**
- ✅ All fields have labels
- ✅ Required fields are indicated
- ✅ Validation errors are announced
- ✅ Error messages are clear
- ✅ Form can be completed with VoiceOver
- ✅ Submit button is accessible

## Testing Scenarios

### Common User Flows

**Test complete user journeys:** Login/signup flow, form submission, navigation between screens, search functionality, content consumption, settings configuration.

### Error Scenarios

**Test error handling:** Invalid input, network errors, validation failures, empty required fields.

### Dynamic Content

**Test content updates:** Loading states, content refreshes, state changes, real-time updates.

## Reporting Issues

### Issue Documentation

**When reporting accessibility issues:**
- Describe the problem clearly
- Include VoiceOver output
- Specify affected elements
- Provide steps to reproduce
- Include expected vs actual behavior

### Issue Template

**Standard issue format:**
```
**Screen:** [Screen name]
**Element:** [Element type and identifier]
**Issue:** [Description of problem]
**VoiceOver Output:** [What VoiceOver reads]
**Expected:** [What should happen]
**Steps to Reproduce:**
1. [Step 1]
2. [Step 2]
3. [Step 3]
**Severity:** [Critical/High/Medium/Low]
```

### Testing Report

**Document test results:** List tested screens, note any issues found, include VoiceOver/iOS version, include device type.

### Best Practices Summary

**Testing Checklist:** All screens tested with VoiceOver, all interactive elements tested, forms tested end-to-end, error scenarios tested, dynamic content tested, contrast ratios verified, color independence verified, focus order verified, announcements verified, tested in light and dark modes, tested on multiple device sizes, issues documented with details, automated tests written where possible, manual testing performed regularly.

### Testing Schedule

**Regular testing:** Test new features before release, test after UI changes, test after refactoring, periodic full app audits, test with each iOS update.
