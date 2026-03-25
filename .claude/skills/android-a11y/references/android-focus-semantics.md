---
name: android-focus-semantics
description: Focus semantics in Android Compose — mergeDescendants, stateDescription, traversalIndex, clearAndSetSemantics, focus order
---

# Focus Semantics — Android Compose

## Purpose

Rules for managing TalkBack focus behavior in Jetpack Compose. Covers semantic grouping, state announcements, custom focus order, and overriding child semantics. Correct semantics let TalkBack users navigate content efficiently and understand element states.

## Table of Contents

- [Default Focus Behavior](#default-focus-behavior)
- [mergeDescendants](#mergedescendants)
- [stateDescription](#statedescription)
- [traversalIndex](#traversalindex)
- [clearAndSetSemantics](#clearandsetsemantics)
- [Custom Toggles with toggleableState](#custom-toggles-with-toggleablestate)
- [Focus Order Principles](#focus-order-principles)
- [Common Violations](#common-violations)

## Related Documents

- [android-content-descriptions.md](./android-content-descriptions.md) — Content descriptions
- [android-headings.md](./android-headings.md) — Heading navigation
- [android-touch-targets.md](./android-touch-targets.md) — Touch target sizing

---

## Default Focus Behavior

TalkBack traverses leaf nodes in the composition tree in top-start to bottom-end order (left-to-right in LTR locales). Each `Text`, `Icon`, `Image`, and interactive composable receives individual focus by default.

```kotlin
// Default — TalkBack focuses on each child separately:
// 1. "Settings icon" (Icon)
// 2. "Account" (Text)
// 3. "Manage your account settings" (Text)
Row {
    Icon(Icons.Default.Person, contentDescription = "Settings icon")
    Column {
        Text("Account")
        Text("Manage your account settings")
    }
}
```

This default behavior is often wrong for card-like content. Use `mergeDescendants` to group related children.

---

## mergeDescendants

`Modifier.semantics { mergeDescendants = true }` collapses all child semantic nodes into one, so TalkBack announces the group as a single item.

### Basic Grouping

```kotlin
// ✅ Group icon + text into a single TalkBack announcement
// TalkBack reads: "Account. Manage your account settings."
Row(
    modifier = Modifier
        .fillMaxWidth()
        .semantics(mergeDescendants = true) { }
        .clickable { onAccountTapped() }
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = null // Let merge handle it
    )
    Spacer(Modifier.width(16.dp))
    Column {
        Text("Account", style = MaterialTheme.typography.titleMedium)
        Text(
            "Manage your account settings",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
```

### List Item Grouping

```kotlin
// ✅ Product list row — merged into one announcement
@Composable
fun ProductListItem(product: Product, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = null, // Part of merged group
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name)
            Text("${product.price}")
            if (product.isOnSale) Text("On sale")
        }
    }
    // TalkBack reads: "[product.name]. [price]. On sale."
}
```

### When NOT to Merge

Do not merge when children have independent actions (e.g., a row with a label and a separate delete button):

```kotlin
// ❌ Do NOT merge — delete button has its own action
Row(modifier = Modifier.semantics(mergeDescendants = true) { }) {
    Text("Shopping list item", modifier = Modifier.weight(1f))
    IconButton(onClick = { onDelete() }) {
        Icon(Icons.Default.Delete, contentDescription = "Delete item")
    }
}

// ✅ Correct — separate focus nodes for label and action
Row(modifier = Modifier.fillMaxWidth()) {
    Text(
        "Shopping list item",
        modifier = Modifier
            .weight(1f)
            .semantics { }
    )
    IconButton(onClick = { onDelete() }) {
        Icon(Icons.Default.Delete, contentDescription = "Delete item")
    }
}
```

---

## stateDescription

`stateDescription` announces the current state of a custom control (toggle, selector, progress) separate from its label.

### Custom Toggle

```kotlin
// ✅ Custom toggle with state announcement
@Composable
fun ToggleRow(
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = if (isEnabled) "On" else "Off"
                role = Role.Switch
            }
            .clickable { onToggle(!isEnabled) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        // Custom visual toggle indicator
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 24.dp)
                .background(
                    if (isEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
    // TalkBack reads: "[label], On, Switch" or "[label], Off, Switch"
}
```

### Selection State

```kotlin
// ✅ Custom radio-like card with state description
@Composable
fun SelectableCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = if (isSelected) "Selected" else "Not selected"
                role = Role.RadioButton
            }
            .clickable(onClick = onClick),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(label, modifier = Modifier.padding(16.dp))
    }
}
```

### Progress State

```kotlin
// ✅ Custom progress indicator
CircularProgressIndicator(
    progress = { progress },
    modifier = Modifier
        .size(48.dp)
        .semantics {
            contentDescription = "Upload progress"
            stateDescription = "${(progress * 100).toInt()}%"
        }
)
```

---

## traversalIndex

`traversalIndex` controls TalkBack's reading order when the visual/layout order differs from the desired announcement order. Lower values are read first. Default is 0f.

```kotlin
// ✅ Badge should be announced after the main label
Row {
    Text(
        "Notifications",
        modifier = Modifier.semantics { traversalIndex = 0f }
    )
    Badge(
        modifier = Modifier.semantics { traversalIndex = 1f }
    ) {
        Text("5")
    }
}
// TalkBack reads: "Notifications", then "5" (badge)

// ✅ Reorder floating action button before content in focus order
Box {
    LazyColumn { /* content */ }
    FloatingActionButton(
        onClick = { onAdd() },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .semantics { traversalIndex = -1f } // Read first despite being last in layout
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add new item")
    }
}
```

Use `traversalIndex` sparingly. Prefer fixing layout order to match reading order when possible.

---

## clearAndSetSemantics

`clearAndSetSemantics { }` removes all descendant semantic nodes and replaces them with only what you define in the block. Use when the default merged announcement is confusing or too verbose.

```kotlin
// ✅ Article card — single clean announcement instead of fragmented children
@Composable
fun ArticleCard(article: Article, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = "${article.title}. ${article.category}. " +
                    "${article.readTimeMinutes} minute read."
                role = Role.Button
            }
    ) {
        Column {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = null
            )
            Text(article.category, style = MaterialTheme.typography.labelSmall)
            Text(article.title, style = MaterialTheme.typography.titleMedium)
            Text("${article.readTimeMinutes} min read")
        }
    }
}

// ✅ Rating display — override individual star announcements
Row(
    modifier = Modifier.clearAndSetSemantics {
        contentDescription = "Rating: ${rating} out of 5 stars"
    }
) {
    repeat(5) { index ->
        Icon(
            imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = null // Cleared by parent
        )
    }
}
```

---

## Custom Toggles with toggleableState

Use `clearAndSetSemantics` with `toggleableState` when building custom toggle controls that need complete semantic control — replacing all child node semantics with a single, precise announcement.

```kotlin
// Custom toggle with full semantic override
Row(
    modifier = Modifier
        .clickable { enabled = !enabled }
        .clearAndSetSemantics {
            stateDescription = if (enabled) "On" else "Off"
            toggleableState = ToggleableState(enabled)
            role = Role.Switch
            contentDescription = "Dark mode"
        }
) {
    Icon(if (enabled) Icons.Filled.DarkMode else Icons.Outlined.DarkMode, null)
    Text("Dark Mode")
}
```

`clearAndSetSemantics` removes all child semantics (icon `contentDescription`, `Text` nodes) and replaces them with the block's declarations. TalkBack announces: "Dark mode, On, Switch" or "Dark mode, Off, Switch". Use this pattern when:
- Child composables would produce redundant or conflicting announcements
- The toggle visual is composed of multiple elements (icon + text + indicator)
- You need `toggleableState` to convey tri-state (on/off/indeterminate) alongside `stateDescription`

---

## Focus Order Principles

TalkBack traverses in the composition order by default (top-start to bottom-end in LTR). Follow these principles:

| Principle | Rule |
|-----------|------|
| Match visual order | Layout order should equal reading order |
| Top-to-bottom | Vertical content reads top first |
| Start-to-end | Horizontal content reads start first (LTR: left; RTL: right) |
| Interactive before passive | Within a group, interactive elements focus first |
| Use `traversalIndex` sparingly | Fix layout order instead when possible |

```kotlin
// ✅ Column — natural top-to-bottom order
Column {
    Text("Step 1: Enter your name")   // Focused first
    TextField(...)                     // Focused second
    Text("Step 2: Enter your email")  // Focused third
    TextField(...)                     // Focused fourth
    Button(onClick = { submit() }) { Text("Continue") } // Last
}
```

---

## Common Violations

| Violation | Symptom | Fix |
|-----------|---------|-----|
| Fragmented list item | TalkBack reads icon, title, subtitle separately | Add `semantics(mergeDescendants = true)` to Row |
| Custom toggle has no state | TalkBack says "On/Off button" without current state | Add `stateDescription = "On"/"Off"` in semantics |
| Wrong reading order | TalkBack reads bottom element before top | Fix layout order or use `traversalIndex` |
| Overly verbose merged group | Merged card reads too many child texts | Use `clearAndSetSemantics` with concise description |
| Interactive children lost in merge | Buttons inside merged group lose their click action | Remove `mergeDescendants` — keep independent focus nodes |
| Missing `role` on custom clickable | TalkBack announces element without role | Add `role = Role.Button` (or appropriate role) in semantics |
