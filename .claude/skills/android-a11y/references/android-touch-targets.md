---
name: android-touch-targets
description: Touch target sizing for Android Compose — 48×48dp minimum, MinimumTouchTargetSize, Box padding patterns, Modifier.clickable
---

# Touch Targets — Android Compose

## Purpose

Rules for ensuring all interactive elements meet the WCAG 2.5.5 Target Size minimum of **48×48dp** in Jetpack Compose. Users with motor impairments, tremors, or low precision rely on adequately-sized tap targets.

## Table of Contents

- [Minimum Size Requirement](#minimum-size-requirement)
- [Material3 Default Behavior](#material3-default-behavior)
- [Custom Composables](#custom-composables)
- [Box Padding Pattern](#box-padding-pattern)
- [Modifier.clickable Sizing](#modifierclickable-sizing)
- [Common Violations](#common-violations)

## Related Documents

- [android-content-descriptions.md](./android-content-descriptions.md) — Content descriptions
- [android-focus-semantics.md](./android-focus-semantics.md) — Semantic grouping

---

## Minimum Size Requirement

WCAG 2.5.5 and Android Material Design both require interactive touch targets to be at least **48×48dp**. This applies to all tappable elements: buttons, checkboxes, switches, icon buttons, list rows, custom clickable composables.

```kotlin
// ✅ Minimum 48×48dp target
Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .clickable { /* action */ }
) {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Settings",
        modifier = Modifier.size(24.dp) // Visual size is 24dp
    )
}
// Touch target: 48×48dp. Visual: 24dp icon centered.
```

---

## Material3 Default Behavior

Material3 components enforce 48×48dp touch targets automatically. Standard components are safe to use without manual sizing:

```kotlin
// ✅ IconButton — Material3 enforces 48×48dp automatically
IconButton(onClick = { onClose() }) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close"
    )
}

// ✅ Button — always meets minimum
Button(onClick = { onSubmit() }) {
    Text("Submit")
}

// ✅ Checkbox — meets minimum by default
Checkbox(
    checked = isChecked,
    onCheckedChange = { isChecked = it }
)

// ✅ Switch — meets minimum by default
Switch(
    checked = isEnabled,
    onCheckedChange = { isEnabled = it }
)

// ✅ RadioButton — meets minimum by default
RadioButton(
    selected = isSelected,
    onClick = { onSelect() }
)
```

Material3's `LocalMinimumInteractiveComponentSize` sets the global minimum to 48dp. This affects all `Indication`-based composables.

---

## Custom Composables

Custom clickable composables do not automatically get 48dp enforcement. You must size them explicitly.

```kotlin
// ❌ Too small — 24dp touch target
Icon(
    imageVector = Icons.Default.Favorite,
    contentDescription = "Add to favorites",
    modifier = Modifier
        .size(24.dp)
        .clickable { onFavorite() }
)

// ✅ Correct — wrap to meet minimum
Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .clickable(
            onClick = { onFavorite() },
            role = Role.Button
        )
) {
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Add to favorites",
        modifier = Modifier.size(24.dp)
    )
}
```

### Reusable Touch Target Wrapper

Extract a helper composable to avoid repetition:

```kotlin
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button
            )
            .semantics { this.contentDescription = contentDescription }
    ) {
        content()
    }
}

// Usage
AccessibleIconButton(
    onClick = { onDelete(item) },
    contentDescription = "Delete ${item.name}"
) {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = null, // Covered by parent
        modifier = Modifier.size(24.dp)
    )
}
```

---

## Box Padding Pattern

When you need a small visual element with a larger tap area, use padding to expand the touch region:

```kotlin
// ✅ Small star icon, large touch target via padding
Icon(
    imageVector = Icons.Default.Star,
    contentDescription = "Toggle favorite",
    modifier = Modifier
        .padding(12.dp) // 24dp icon + 12dp padding on each side = 48dp touch area
        .size(24.dp)
        .clickable(role = Role.Button) { onToggleFavorite() }
)

// ✅ Text link with adequate touch area
Text(
    text = "Forgot password?",
    color = MaterialTheme.colorScheme.primary,
    style = MaterialTheme.typography.bodyMedium,
    modifier = Modifier
        .padding(vertical = 12.dp, horizontal = 8.dp) // Expand touch area
        .clickable(role = Role.Button) { onForgotPassword() }
)
```

Note: padding applied **before** `clickable` extends the tappable area. Padding applied after does not.

---

## Modifier.clickable Sizing

When adding `clickable` to a composable, ensure the element's total size (including padding) is at least 48×48dp:

```kotlin
// ✅ Row item — full-width row is always large enough vertically
Row(
    modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp) // Ensure minimum height
        .clickable { onItemSelected(item) }
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text(item.label, modifier = Modifier.weight(1f))
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null
    )
}

// ✅ Chip / tag with minimum height
FilterChip(
    selected = isSelected,
    onClick = { onToggle() },
    label = { Text(label) },
    modifier = Modifier.heightIn(min = 48.dp)
)

// ❌ Small image used as a button — fails touch target
Image(
    painter = painterResource(R.drawable.ic_close),
    contentDescription = "Close",
    modifier = Modifier
        .size(16.dp)
        .clickable { onClose() } // 16dp — too small
)
```

### Role Annotation

Always specify `Role.Button` (or the appropriate role) when using `Modifier.clickable` on non-button composables:

```kotlin
Modifier.clickable(role = Role.Button) { action() }
Modifier.clickable(role = Role.Checkbox) { toggle() }
Modifier.clickable(role = Role.Switch) { toggle() }
Modifier.clickable(role = Role.Tab) { selectTab() }
```

---

## Common Violations

| Violation | Cause | Fix |
|-----------|-------|-----|
| Icon button under 48dp | Custom `Icon` with small size + `clickable` | Wrap in `Box` with `sizeIn(minWidth = 48.dp, minHeight = 48.dp)` |
| Small text link | Bare `Text` with `clickable` and no padding | Add `Modifier.padding(vertical = 12.dp)` before `clickable` |
| Tight list row | Row `height` set to 32dp | Set `heightIn(min = 48.dp)` on the Row |
| Toggle with small hit area | Custom toggle using `Canvas` | Wrap canvas in `Box` meeting 48×48dp |
| Padding applied after clickable | `clickable` then `padding` | Reverse order: `padding` then `clickable` |
