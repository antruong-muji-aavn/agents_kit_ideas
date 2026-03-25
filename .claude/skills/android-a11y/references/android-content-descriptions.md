---
name: android-content-descriptions
description: Content descriptions for Android Compose — meaningful vs decorative, images, icons, custom composables, common violations
---

# Content Descriptions — Android Compose

## Purpose

Rules for providing TalkBack-compatible content descriptions in Jetpack Compose. Covers meaningful images, decorative images, actionable icons, and custom composables that need explicit semantic annotations.

## Table of Contents

- [Basics](#basics)
- [Meaningful Images](#meaningful-images)
- [Decorative Images](#decorative-images)
- [Actionable Icons](#actionable-icons)
- [Custom Composables](#custom-composables)
- [Common Violations](#common-violations)
- [Live Regions](#live-regions)
- [Custom Accessibility Actions](#custom-accessibility-actions)

## Related Documents

- [android-touch-targets.md](./android-touch-targets.md) — Touch target sizing
- [android-focus-semantics.md](./android-focus-semantics.md) — Semantic grouping and state
- [android-headings.md](./android-headings.md) — Heading structure

---

## Basics

TalkBack reads `contentDescription` when announcing elements. If `contentDescription` is `null` on an `Image`, Compose uses the image's resource name — which is rarely user-friendly. Always set it explicitly.

```kotlin
// ✅ Explicit content description
Image(
    painter = painterResource(R.drawable.ic_profile),
    contentDescription = "User profile photo"
)

// ❌ Avoid — TalkBack may read the resource name
Image(
    painter = painterResource(R.drawable.ic_profile),
    contentDescription = null // Wrong for informative images
)
```

---

## Meaningful Images

### Informative Images

Images that convey information must have a `contentDescription` describing what the image communicates — not its appearance.

```kotlin
// ✅ Describe the content, not the file name
Image(
    painter = painterResource(R.drawable.chart_sales_q4),
    contentDescription = "Sales chart showing 30% growth in Q4"
)

// ✅ User avatar — include name when available
Image(
    painter = rememberAsyncImagePainter(user.avatarUrl),
    contentDescription = "${user.name}'s profile photo"
)

// ❌ Avoid generic or filename-based descriptions
Image(
    painter = painterResource(R.drawable.ic_user),
    contentDescription = "ic_user" // File name is not a description
)

// ❌ Avoid prefixing with "Image of"
Image(
    painter = painterResource(R.drawable.chart_sales_q4),
    contentDescription = "Image of a chart" // Redundant prefix
)
```

### Dynamic Content Descriptions

When image content changes at runtime, derive the description from the data:

```kotlin
@Composable
fun ProductImage(product: Product) {
    Image(
        painter = rememberAsyncImagePainter(product.imageUrl),
        contentDescription = "${product.name} product photo",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    )
}
```

### Complex Images — Charts and Diagrams

For charts and diagrams, describe the data insight, not the chart type:

```kotlin
// ✅ Describe the data insight
Image(
    painter = painterResource(R.drawable.revenue_chart),
    contentDescription = "Monthly revenue chart: grew from $10K in January to $18K in June"
)

// ✅ For very complex charts, use clearAndSetSemantics for a longer description
Box(
    modifier = Modifier
        .fillMaxWidth()
        .semantics {
            contentDescription = "Revenue trend chart. Revenue increased steadily " +
                "from $10,000 in January to $18,000 in June, a 80% increase over 6 months."
        }
) {
    Chart(data = revenueData)
}
```

---

## Decorative Images

Decorative images (backgrounds, dividers, decorative icons) must have `contentDescription = null` so TalkBack skips them.

```kotlin
// ✅ Decorative — set null explicitly
Image(
    painter = painterResource(R.drawable.background_pattern),
    contentDescription = null // Decorative — TalkBack skips it
)

// ✅ Decorative icon next to labeled text
Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null, // Decorative — label is in the Text
        tint = Color.Yellow
    )
    Text("Top Rated")
}

// ✅ Divider line — purely decorative
HorizontalDivider() // No contentDescription needed — built-in composable
```

### When to Mark Decorative

Mark as decorative when:
- Image adds visual flair but no information
- A nearby `Text` already communicates the same information
- Image is a background, border, or pattern
- Icon is paired with a visible text label that fully describes the action

---

## Actionable Icons

When an icon is the only content inside a clickable element, describe the **action** it performs, not the icon name.

```kotlin
// ✅ Describe the action
IconButton(onClick = { onClose() }) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close dialog"
    )
}

// ✅ Favorite toggle — describe the state change action
IconButton(onClick = { onFavoriteToggle() }) {
    Icon(
        imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites"
    )
}

// ✅ Navigation action
IconButton(onClick = { onBack() }) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Navigate back"
    )
}

// ❌ Avoid describing the icon name
IconButton(onClick = { onClose() }) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "X icon" // Describes appearance, not action
    )
}

// ❌ Avoid vague descriptions
IconButton(onClick = { onShare() }) {
    Icon(
        imageVector = Icons.Default.Share,
        contentDescription = "Button" // No information
    )
}
```

### Standard Icon Labels

Common icon-to-action mappings:

| Icon | Label |
|------|-------|
| `Icons.Default.Close` / X | "Close" or "Close [dialog name]" |
| `Icons.Default.Delete` / Trash | "Delete" or "Delete [item name]" |
| `Icons.Default.Edit` / Pencil | "Edit" or "Edit [item name]" |
| `Icons.Default.Search` | "Search" |
| `Icons.Default.Share` | "Share" |
| `Icons.Default.MoreVert` | "More options" |
| `Icons.Default.Add` / Plus | "Add" or "Add [item type]" |
| `Icons.AutoMirrored.Filled.ArrowBack` | "Navigate back" |

---

## Custom Composables

Custom composables that don't use built-in `Image` or `Icon` need explicit content descriptions via `Modifier.semantics`.

```kotlin
// ✅ Custom image composable
@Composable
fun AvatarImage(
    imageUrl: String,
    userName: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "$userName's avatar",
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
    )
}

// ✅ Canvas-drawn element with semantic annotation
@Composable
fun SignalStrengthIndicator(
    bars: Int,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(24.dp)
            .semantics {
                contentDescription = "Signal strength: $bars out of 4 bars"
            }
    ) {
        // draw signal bars
    }
}

// ✅ Card with image that uses clearAndSetSemantics
@Composable
fun ArticleCard(article: Article, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = article.title
                role = Role.Button
            }
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(article.imageUrl),
                contentDescription = null // Covered by clearAndSetSemantics above
            )
            Text(article.title)
        }
    }
}
```

---

## Common Violations

| Violation | Symptom | Fix |
|-----------|---------|-----|
| Missing contentDescription on informative Image | TalkBack reads resource filename | Add `contentDescription = "..."` describing the content |
| `contentDescription = null` on icon-only button | TalkBack says nothing for the button | Add action-describing `contentDescription` to `Icon` |
| "Image of …" prefix in description | Redundant announcement | Remove prefix — TalkBack already says "Image" |
| Decorative icon has description | Duplicate TalkBack announcement with nearby text | Set `contentDescription = null` on decorative icon |
| Generic labels like "icon", "button" | Non-descriptive TalkBack announcement | Replace with action or content description |
| Resource filename as description | Reads "ic_user_profile" aloud | Replace with human-readable description |

---

## Live Regions

Live regions automatically announce dynamic content changes to TalkBack without requiring the user to navigate to the element.

```kotlin
// Polite: announced after current speech finishes
Text(
    text = "$count items in cart",
    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
)

// Assertive: interrupts current speech immediately
Text(
    text = "Error: payment failed",
    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }
)
```

Use `Polite` for status updates (cart count, search results count, progress messages). Use `Assertive` for errors and critical alerts that require immediate attention. Prefer `Polite` by default — `Assertive` interrupts the user and should be reserved for time-sensitive errors.

---

## Custom Accessibility Actions

`customActions` adds named actions to the TalkBack actions menu for complex widgets whose interactions cannot be conveyed by standard semantics alone.

```kotlin
Slider(
    value = volume,
    onValueChange = { volume = it },
    modifier = Modifier.semantics {
        contentDescription = "Volume slider"
        stateDescription = "${(volume * 100).toInt()} percent"
        customActions = listOf(
            CustomAccessibilityAction("Increase volume") { /* action */ true },
            CustomAccessibilityAction("Decrease volume") { /* action */ true }
        )
    }
)
```

Use `customActions` when:
- A widget supports swipe gestures or drag interactions that TalkBack cannot discover automatically
- A list item has multiple actions (edit, delete, share) that need to be accessible without visible buttons
- A custom control has domain-specific operations not covered by built-in roles
