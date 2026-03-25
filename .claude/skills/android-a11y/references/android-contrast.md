---
name: android-contrast
description: Color contrast for Android Compose — WCAG AA ratios, MaterialTheme, dark mode, Material You dynamic color, testing tools
---

# Color Contrast — Android Compose

## Purpose

Rules for meeting WCAG 2.1 AA color contrast requirements in Jetpack Compose. Covers contrast ratios for text and UI components, MaterialTheme color system usage, dark mode validation, and dynamic color (Material You) accessibility considerations.

## Table of Contents

- [WCAG AA Contrast Requirements](#wcag-aa-contrast-requirements)
- [MaterialTheme Color System](#materialtheme-color-system)
- [Dark Mode Contrast](#dark-mode-contrast)
- [Dynamic Color — Material You](#dynamic-color--material-you)
- [Testing Approaches](#testing-approaches)
- [Common Violations](#common-violations)

## Related Documents

- [android-content-descriptions.md](./android-content-descriptions.md) — Content descriptions
- [android-focus-semantics.md](./android-focus-semantics.md) — Semantics

---

## WCAG AA Contrast Requirements

WCAG 2.1 AA requires minimum contrast ratios between foreground (text/icon) and background colors:

| Element Type | Minimum Ratio | Notes |
|-------------|--------------|-------|
| Normal text (< 18pt / < 14pt bold) | **4.5:1** | Body text, labels, captions |
| Large text (≥ 18pt or ≥ 14pt bold) | **3:1** | Headlines, large buttons |
| UI components (borders, icons, focus rings) | **3:1** | Non-text visual indicators |
| Decorative elements | None | Images, illustrations without informational content |
| Disabled elements | None | WCAG exempts disabled UI |
| Placeholder text | **4.5:1** | Placeholder is informational |

In Compose, `MaterialTheme.typography` body styles are typically 14–16sp (normal text), requiring **4.5:1**.

```kotlin
// ✅ Compute approximate contrast ratio (for reference)
// Contrast ratio = (L1 + 0.05) / (L2 + 0.05)
// where L1 = relative luminance of lighter color, L2 = darker
// Use tools to verify exact ratios (see Testing section)

// ✅ White text on Material3 primary (typically sufficient)
Text(
    text = "Submit",
    color = MaterialTheme.colorScheme.onPrimary,    // White
    modifier = Modifier.background(MaterialTheme.colorScheme.primary) // Primary color
)

// ✅ Surface text on surface background
Text(
    text = "Card title",
    color = MaterialTheme.colorScheme.onSurface,    // Dark text
    modifier = Modifier.background(MaterialTheme.colorScheme.surface) // Light background
)
```

---

## MaterialTheme Color System

Material3's color scheme pairs are designed to meet 4.5:1 contrast by default. Always use the `on*` color with its paired `*` background:

| Background Token | Text/Icon Token | Usage |
|-----------------|----------------|-------|
| `primary` | `onPrimary` | Primary buttons, FABs |
| `primaryContainer` | `onPrimaryContainer` | Filled tonal buttons, chips |
| `secondary` | `onSecondary` | Secondary actions |
| `secondaryContainer` | `onSecondaryContainer` | Tonal secondary elements |
| `surface` | `onSurface` | Cards, sheets, dialogs |
| `surfaceVariant` | `onSurfaceVariant` | Outlined cards, input fields |
| `error` | `onError` | Error indicators |
| `errorContainer` | `onErrorContainer` | Error backgrounds |

```kotlin
// ✅ Always use paired on* colors
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
) {
    Text("Featured Item") // Uses contentColor = onPrimaryContainer
}

// ✅ Outlined input field — border uses onSurfaceVariant
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Email") },
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
)

// ❌ Hardcoded colors that may fail contrast in both light/dark modes
Text(
    text = "Warning",
    color = Color(0xFFFFAA00), // Orange — fails 4.5:1 on white at small sizes
    modifier = Modifier.background(Color.White)
)
```

### Secondary Text and Captions

Secondary/muted text (using `onSurfaceVariant`) must still meet 4.5:1 for body-size text:

```kotlin
// ✅ Secondary text using a16y-safe token
Text(
    text = "Last updated 3 minutes ago",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant // Verify ratio with your theme
)

// ❌ Semi-transparent text — may fail contrast
Text(
    text = "Optional",
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) // 40% opacity likely fails
)
```

---

## Dark Mode Contrast

All color decisions must be verified in both light and dark mode. MaterialTheme handles this automatically when using semantic color tokens — but custom colors must be defined for both.

```kotlin
// ✅ Define custom colors for both modes using CompositionLocalProvider or dynamic resource
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A6B3C),       // Dark green — passes on white
    onPrimary = Color.White,
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF68D98A),        // Light green — passes on dark surface
    onPrimary = Color(0xFF003919),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colors, content = content)
}
```

### Preview Both Modes

Always include dark mode previews during development:

```kotlin
@Preview(name = "Light Mode", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyComponentPreview() {
    AppTheme {
        MyComponent()
    }
}
```

### Focus Indicators in Dark Mode

Custom focus ring colors must also meet 3:1 contrast against their background in dark mode:

```kotlin
// ✅ Focus indicator adapts to theme
val focusColor = MaterialTheme.colorScheme.primary // Adjusts per light/dark scheme

Box(
    modifier = Modifier
        .border(
            width = 2.dp,
            color = if (isFocused) focusColor else Color.Transparent,
            shape = RoundedCornerShape(8.dp)
        )
) { /* content */ }
```

---

## Dynamic Color — Material You

Material You (Android 12+) generates a color scheme from the user's wallpaper. Dynamically generated colors can produce unexpected contrast failures because you don't control the palette.

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

### Dynamic Color Risks

| Risk | Mitigation |
|------|-----------|
| Generated primary may be low contrast on surface | Always use `onPrimary` / `onSurface` — Material3 guarantees pairs meet AA |
| Custom hardcoded colors clash with dynamic palette | Replace hardcoded colors with semantic tokens |
| Error states use red that bleeds with dynamic purple | Always use `colorScheme.error` / `colorScheme.onError` tokens |
| Third-party content (images) may not contrast with dynamic bg | Don't rely on image-derived colors for text |

### What Material3 Guarantees

When using only `colorScheme.*` tokens correctly (always pairing `primary` with `onPrimary` etc.), Material3's color generation algorithm **guarantees 4.5:1** for those pairs. Do not override individual tokens without verifying the resulting pair contrast.

---

## Testing Approaches

### Accessibility Scanner (Google)

Google's Accessibility Scanner app checks contrast ratios on your screen:

1. Install Accessibility Scanner from Google Play
2. Grant overlay permission
3. Open your app and tap the Scanner FAB
4. Review flagged contrast issues with exact ratios

### Android Studio Layout Inspector

Layout Inspector shows computed colors. Combine with an online contrast checker:

1. Run app → Open Layout Inspector
2. Select a `Text` node → note `textColor` and `background` values
3. Enter hex values at [webaim.org/resources/contrastchecker](https://webaim.org/resources/contrastchecker)

### Compose Preview with Contrast Plugin

Use the "Color Contrast Analyzer" plugin in Android Studio to check preview screenshots.

### Manual Testing Checklist

```
[ ] Text passes 4.5:1 in light mode
[ ] Text passes 4.5:1 in dark mode
[ ] Large text (≥ 18sp) passes 3:1 in both modes
[ ] Icon-only buttons pass 3:1 against their background
[ ] Error message text passes 4.5:1 against error background
[ ] Placeholder text passes 4.5:1
[ ] Focus ring passes 3:1 against background
[ ] No color is the sole differentiator (use icons or patterns too)
[ ] Tested with dynamic color on a device running Android 12+
```

### Disable Dynamic Color for Testing

To test with your fallback colors (not device wallpaper):

```kotlin
// Temporarily force non-dynamic for testing
AppTheme(dynamicColor = false) {
    AppContent()
}
```

---

## Common Violations

| Violation | Cause | Fix |
|-----------|-------|-----|
| Gray text on white background fails 4.5:1 | Light gray (`#AAAAAA`) on white (#FFFFFF) = 2.3:1 | Darken to at least `#767676` for 4.5:1 |
| Secondary text too faint in dark mode | Using fixed alpha (e.g. `onSurface.copy(alpha = 0.38f)`) | Use `onSurfaceVariant` token instead |
| Error red fails contrast | Custom error red too light on white | Use `colorScheme.error` / `colorScheme.onError` |
| Icon-only button fails 3:1 | Icon color not sufficiently dark | Use `onSurface` or check custom icon tint |
| Color alone conveys meaning | Red = error, green = success — no other indicator | Add icon, label, or pattern alongside color |
| Dynamic color creates new failing pair | Wallpaper-derived color breaks custom component | Replace hardcoded colors with semantic tokens |
