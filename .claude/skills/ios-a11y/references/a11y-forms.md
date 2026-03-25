---
name: a11y-forms
description: Form input accessibility — labels, validation, error states, input types, form structure
---

# Form Accessibility Rules

## Purpose

Accessibility rules for form inputs, text fields, and interactive form elements, ensuring VoiceOver users can complete forms successfully.

## Table of Contents

- [Text Fields](#text-fields)
- [Labels and Placeholders](#labels-and-placeholders)
- [Form Validation](#form-validation)
- [Input Types](#input-types)
- [Form Structure](#form-structure)
- [Error Messages](#error-messages)

## Related Documents

- [a11y-core](./a11y-core.md) - Core accessibility principles
- [a11y-buttons](./a11y-buttons.md) - Button accessibility
- [a11y-headings](./a11y-headings.md) - Heading structure

## Text Fields

### UITextField Accessibility

**Default Behavior:**
- `UITextField` is accessible by default
- Placeholder text may be read, but shouldn't be relied upon
- Always provide explicit accessibility label
- Use `.searchField` trait for search inputs

```swift
// ✅ Text field with label
let emailField = UITextField()
emailField.placeholder = "Enter email"
emailField.accessibilityLabel = "Email address"
// VoiceOver reads: "Email address, Text field"

// ✅ Search field
let searchField = UITextField()
searchField.accessibilityLabel = "Search"
searchField.accessibilityTraits = .searchField
```

### Required Fields

**Indicate required fields:**
- Include "required" in label or hint
- Use visual indicator (asterisk) with accessible label
- Announce required status clearly

```swift
// ✅ Required field
let nameField = UITextField()
nameField.accessibilityLabel = "Name"
nameField.accessibilityHint = "Required field"

// ✅ With visual indicator
let emailLabel = UILabel()
emailLabel.text = "Email *"
emailLabel.accessibilityLabel = "Email, Required"

let emailField = UITextField()
emailField.accessibilityLabel = "Email address"
emailField.accessibilityHint = "Required field"
```

### Text Field Labels

**Label Guidelines:**
- Describe what information is expected
- Match or complement placeholder text
- Be specific ("Email address" not "Email field")

```swift
// ✅ Good labels
textField.accessibilityLabel = "Email address"
textField.accessibilityLabel = "Phone number"
textField.accessibilityLabel = "Password"

// ❌ Bad labels
textField.accessibilityLabel = "Text field" // Too generic
textField.accessibilityLabel = "Input" // Not descriptive
textField.accessibilityLabel = "Email field" // Redundant
```

## Labels and Placeholders

### Label Association

**Connect labels to fields:**
- Use `UILabel` with proper association
- Set `accessibilityLabel` on text field
- Don't rely solely on placeholder text

```swift
// ✅ Label association
let nameLabel = UILabel()
nameLabel.text = "Name"
nameLabel.isAccessibilityElement = false // Label is for visual only

let nameField = UITextField()
nameField.accessibilityLabel = "Name"
nameField.placeholder = "Enter your name"
```

### Placeholder Text

**Placeholder limitations:**
- Placeholder may not be read reliably
- Always provide `accessibilityLabel`
- Use placeholder for visual hint only
- Hint can complement placeholder

```swift
// ✅ Proper placeholder usage
let emailField = UITextField()
emailField.placeholder = "example@email.com"
emailField.accessibilityLabel = "Email address"
emailField.accessibilityHint = "Enter your email address"

// ❌ Don't rely on placeholder
let field = UITextField()
field.placeholder = "Email" // May not be read by VoiceOver
// Missing accessibilityLabel
```

### Hint Guidelines

**When to use hints:**
- Explain format requirements
- Provide input examples
- Clarify what happens after input

```swift
// ✅ Helpful hints
emailField.accessibilityHint = "Enter your email address"
phoneField.accessibilityHint = "Enter 10-digit phone number"
passwordField.accessibilityHint = "Must be at least 8 characters"
```

## Form Validation

### Real-time Validation

**Announce validation errors:**
- Use `UIAccessibility.post()` for errors
- Update `accessibilityValue` with error state
- Don't interrupt typing unnecessarily

```swift
// ✅ Validation announcement
func validateEmail(_ email: String) {
    if isValidEmail(email) {
        emailField.accessibilityValue = nil
    } else {
        emailField.accessibilityValue = "Invalid email format"
        UIAccessibility.post(
            notification: .announcement,
            argument: "Invalid email format"
        )
    }
}
```

### Error States

**Indicate error visually and accessibly:**
- Set error message as `accessibilityValue`
- Use `.notEnabled` trait only if field is disabled
- Announce errors when field loses focus

```swift
// ✅ Error state
func showError(_ message: String, for field: UITextField) {
    field.accessibilityValue = message
    UIAccessibility.post(
        notification: .announcement,
        argument: message
    )
}

func clearError(for field: UITextField) {
    field.accessibilityValue = nil
}
```

## Input Types

### Secure Text Fields

**Password fields:**
- Use `UITextField` with `isSecureTextEntry = true`
- Label should indicate password field
- Don't reveal password in accessibility value

```swift
// ✅ Password field
let passwordField = UITextField()
passwordField.isSecureTextEntry = true
passwordField.accessibilityLabel = "Password"
passwordField.accessibilityHint = "Enter your password"
// VoiceOver reads: "Password, Secure text field"
```

### Number Inputs

**Numeric fields:**
- Use appropriate keyboard type
- Label should indicate numeric input
- Hint can specify format or range

```swift
// ✅ Number field
let ageField = UITextField()
ageField.keyboardType = .numberPad
ageField.accessibilityLabel = "Age"
ageField.accessibilityHint = "Enter your age in years"

// ✅ Phone number
let phoneField = UITextField()
phoneField.keyboardType = .phonePad
phoneField.accessibilityLabel = "Phone number"
phoneField.accessibilityHint = "Enter 10-digit phone number"
```

### Date Pickers

**UIDatePicker:**
- Accessible by default
- Has `.adjustable` trait
- VoiceOver reads current value and allows adjustment

```swift
// ✅ Date picker (accessible by default)
let datePicker = UIDatePicker()
datePicker.datePickerMode = .date
// VoiceOver reads: "Date picker, Adjustable"
// User can swipe up/down to adjust
```

### Picker Views

**UIPickerView:** Implement `UIPickerViewAccessibilityDelegate`, provide labels for each component, describe what each component represents.

## Form Structure

### Form Sections

**Organize forms logically:** Use headings to separate sections, group related fields, provide form-level instructions.

### Field Order

**Logical tab order:** Ensure fields are in logical order, VoiceOver navigates in view hierarchy order, test with VoiceOver to verify order.

### Form Instructions

**Provide form-level guidance:** Use hint or announcement for instructions, explain required fields at form level, announce completion status.

## Error Messages

### Error Announcements

**Announce errors clearly:**
- Use `UIAccessibility.post()` for immediate errors
- Set error message as `accessibilityValue`
- Don't rely solely on visual error indicators

```swift
// ✅ Error announcement
func validateForm() {
    if emailField.text?.isEmpty ?? true {
        let errorMessage = "Email address is required"
        emailField.accessibilityValue = errorMessage
        UIAccessibility.post(
            notification: .announcement,
            argument: errorMessage
        )
    }
}
```

### Error Labels

**Associate errors with fields:** Error message should reference field name. Use clear, actionable language. Provide guidance on how to fix. **Good:** "Email address is required", "Invalid email format". **Bad:** "Error", "Wrong".

### Error Summary

**Form-level error summary:** Announce total number of errors, list fields with errors, help users navigate to errors.

### Best Practices Summary

**Form Accessibility Checklist:** All fields have `accessibilityLabel`, required fields are clearly indicated, placeholder text is not relied upon, input types are clearly labeled, validation errors are announced, error messages are specific and actionable, form structure uses headings, field order is logical, secure fields use appropriate traits, all labels and hints are localized.
