---
name: android-headings
description: Heading semantics in Android Compose — heading() modifier, hierarchy, TalkBack heading navigation
---

# Headings — Android Compose

## Purpose

Rules for marking section headings in Jetpack Compose so TalkBack users can navigate content efficiently using heading-swipe gestures. Correct heading structure is essential for long screens with multiple sections.

## Table of Contents

- [Heading Basics](#heading-basics)
- [Applying the heading() Semantic](#applying-the-heading-semantic)
- [Heading Hierarchy](#heading-hierarchy)
- [TalkBack Heading Navigation](#talkback-heading-navigation)
- [Dynamic Headings](#dynamic-headings)
- [Common Violations](#common-violations)

## Related Documents

- [android-focus-semantics.md](./android-focus-semantics.md) — Focus order and mergeDescendants
- [android-content-descriptions.md](./android-content-descriptions.md) — Content descriptions

---

## Heading Basics

In Jetpack Compose, any `Text` can be designated as a heading by applying `Modifier.semantics { heading() }`. This is the Compose equivalent of HTML `<h1>`–`<h6>` tags. TalkBack announces the element as "Heading" and allows users to jump between headings using swipe-up/swipe-down gestures in heading navigation mode.

```kotlin
// ✅ Minimal heading example
Text(
    text = "Account Settings",
    style = MaterialTheme.typography.headlineMedium,
    modifier = Modifier.semantics { heading() }
)
// TalkBack reads: "Account Settings, Heading"

// ❌ Visual heading without semantic — TalkBack cannot navigate to it
Text(
    text = "Account Settings",
    style = MaterialTheme.typography.headlineMedium // Looks like a heading, but isn't one
)
```

---

## Applying the heading() Semantic

### Direct Modifier

```kotlin
Text(
    text = "Notifications",
    style = MaterialTheme.typography.titleLarge,
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .semantics { heading() }
)
```

### Reusable Section Header Composable

Extract a composable to avoid repeating the semantics block:

```kotlin
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { heading() }
    )
}

// Usage
SectionHeader("General")
SectionHeader("Privacy & Security")
SectionHeader("Notifications")
```

### Inside LazyColumn

Headings inside `LazyColumn` are supported — Compose handles semantics across all lazy items:

```kotlin
LazyColumn {
    item {
        SectionHeader("Recent Orders")
    }
    items(recentOrders) { order ->
        OrderListItem(order)
    }
    item {
        SectionHeader("Past Orders")
    }
    items(pastOrders) { order ->
        OrderListItem(order)
    }
}
```

---

## Heading Hierarchy

Compose does not have built-in heading levels (H1–H6) like HTML. Use visual style to convey hierarchy and reserve `heading()` for all section titles regardless of level. Follow these conventions:

| Level | Style | Usage |
|-------|-------|-------|
| H1 | `headlineLarge` / `headlineMedium` | Screen or page title (one per screen) |
| H2 | `titleLarge` | Major section titles |
| H3 | `titleMedium` | Subsection titles within a section |
| H4 | `titleSmall` | Minor group labels within a subsection |

All levels should use `Modifier.semantics { heading() }`.

```kotlin
@Composable
fun ProfileScreen(user: User) {
    LazyColumn {
        // H1 — Screen title
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .semantics { heading() }
            )
        }

        // H2 — Major section
        item {
            SectionHeader("Personal Information") // Uses titleMedium + heading()
        }
        item { NameRow(user.name) }
        item { EmailRow(user.email) }

        // H2 — Another major section
        item {
            SectionHeader("Account Security")
        }
        item { PasswordRow() }
        item { TwoFactorRow() }

        // H2 — Another major section
        item {
            SectionHeader("Preferences")
        }

        // H3 — Subsection inside Preferences
        item {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .semantics { heading() }
            )
        }
        item { EmailNotificationsRow() }
        item { PushNotificationsRow() }
    }
}
```

### Heading Hierarchy Rules

- One H1 per screen — the screen's primary title
- H2 sections group related content
- Don't skip levels (H1 directly to H3 leaves a gap)
- Every distinct content group should have a heading
- Don't mark every `Text` as a heading — only section titles

```kotlin
// ❌ Skipping heading levels
Text("App Settings", modifier = Modifier.semantics { heading() })  // H1
// No H2...
Text("Notification Sounds", modifier = Modifier.semantics { heading() }) // Should be H2, not jump here

// ✅ Correct progression
Text("App Settings", modifier = Modifier.semantics { heading() })  // H1
Text("Notifications", modifier = Modifier.semantics { heading() }) // H2
Text("Notification Sounds", modifier = Modifier.semantics { heading() }) // H3
```

---

## TalkBack Heading Navigation

TalkBack's heading navigation allows users to swipe up/down while in "Headings" reading mode to jump between headings without traversing every element.

**User gesture sequence:**
1. Open TalkBack reading controls (swipe up + right or use volume key shortcut)
2. Select "Headings" from reading control options
3. Swipe down to move to next heading
4. Swipe up to move to previous heading

For this to work, headings must be marked with `Modifier.semantics { heading() }`. Headings styled only with large text or bold font are invisible to TalkBack's heading navigation.

```kotlin
// ✅ Settings screen optimized for heading navigation
@Composable
fun SettingsScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            // Screen title — always include as H1
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .semantics { heading() }
            )
        }

        // Each section has a heading — TalkBack users can jump directly to any section
        settingsSections.forEach { section ->
            item {
                SectionHeader(section.title)
            }
            items(section.items) { item ->
                SettingsRow(item)
            }
        }
    }
}
```

---

## Dynamic Headings

When screen content changes (tab switch, filter applied, data reload), headings should reflect the new content state:

```kotlin
// ✅ Tab content with dynamic section headings
@Composable
fun OrdersScreen(selectedTab: OrderTab) {
    Column {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            OrderTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.label) }
                )
            }
        }

        // Heading updates when tab changes
        Text(
            text = when (selectedTab) {
                OrderTab.ACTIVE -> "Active Orders"
                OrderTab.COMPLETED -> "Completed Orders"
                OrderTab.CANCELLED -> "Cancelled Orders"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics { heading() }
        )

        // Content for selected tab
        OrderList(tab = selectedTab)
    }
}
```

---

## Common Violations

| Violation | Symptom | Fix |
|-----------|---------|-----|
| Section title without `heading()` | TalkBack cannot jump to section | Add `Modifier.semantics { heading() }` |
| Every `Text` marked as heading | Heading navigation is cluttered | Reserve `heading()` for section titles only |
| No H1 on screen | Screen has no entry point for heading nav | Add screen title with `heading()` |
| Skipped heading levels | Logical gap in content hierarchy | Add intermediate level headings |
| Heading in dynamic content not updated | Stale heading after content reload | Derive heading text from current data state |
| Bold/large text assumed to be heading | TalkBack ignores styling for heading nav | Always add `Modifier.semantics { heading() }` |
