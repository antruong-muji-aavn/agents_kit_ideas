---
name: android-views-xml-a11y
description: Accessibility for Android View-based UI — XML layouts, custom Views, RecyclerView, ViewHolder, AccessibilityDelegate
---

# Android View/XML Accessibility

## Purpose

Accessibility rules for traditional View-based Android UI. The epostSdk and app modules use Fragments, Activities, RecyclerView, and custom Views (not Compose) — this reference covers those patterns. For Compose, see [android-content-descriptions.md](./android-content-descriptions.md).

## Table of Contents

- [XML Layout Attributes](#xml-layout-attributes)
- [Custom Views](#custom-views)
- [RecyclerView & ViewHolder](#recyclerview--viewholder)
- [Accessibility Delegate](#accessibility-delegate)
- [Common Violations](#common-violations)
- [Related Documents](#related-documents)

---

## XML Layout Attributes

### contentDescription

TalkBack reads `contentDescription` for images, icons, and custom views that have no text label.

```xml
<!-- ✅ Image with meaningful description -->
<ImageView
    android:contentDescription="User profile photo"
    android:importantForAccessibility="yes" />

<!-- ✅ ImageButton — describe the action, not the icon -->
<ImageButton
    android:contentDescription="Delete letter"
    android:src="@drawable/ic_delete" />

<!-- ✅ Decorative image — hide from TalkBack -->
<ImageView
    android:contentDescription="@null"
    android:importantForAccessibility="no" />

<!-- ❌ Missing description — TalkBack reads resource name "ic_delete" -->
<ImageButton android:src="@drawable/ic_delete" />
```

### importantForAccessibility

Controls whether TalkBack visits the view.

| Value | Behaviour |
|-------|-----------|
| `yes` | Always visited, even if it has no content |
| `no` | Never visited by TalkBack |
| `noHideDescendants` | Neither this view nor any child is visited |
| `auto` (default) | System decides — visited if it has a label or is interactive |

```xml
<!-- ✅ Decorative divider — completely hidden -->
<View android:importantForAccessibility="no" />

<!-- ✅ Suppress entire subtree (e.g. animated background) -->
<FrameLayout android:importantForAccessibility="noHideDescendants">
    <!-- animated decorative views here -->
</FrameLayout>

<!-- ✅ Force-visible icon that would otherwise be skipped -->
<ImageView
    android:importantForAccessibility="yes"
    android:contentDescription="Unread indicator" />
```

### labelFor — Form Field Association

Link a label `TextView` to its `EditText` so TalkBack reads the label when the field is focused.

```xml
<!-- ✅ Correct: label is associated with field -->
<TextView
    android:id="@+id/label_name"
    android:text="Full name"
    android:labelFor="@id/input_name" />

<EditText
    android:id="@+id/input_name"
    android:hint="Enter your full name" />

<!-- ❌ Wrong: standalone hint is read but loses context when field is focused -->
<EditText android:hint="Full name" />
```

### accessibilityLiveRegion — Dynamic Text Updates

Announce text changes without requiring user to navigate to the view.

```xml
<!-- ✅ Polite: announces after current speech finishes -->
<TextView
    android:id="@+id/status_message"
    android:accessibilityLiveRegion="polite" />

<!-- ✅ Assertive: interrupts current speech (use sparingly — errors, critical alerts) -->
<TextView
    android:id="@+id/error_message"
    android:accessibilityLiveRegion="assertive" />
```

### Grouping for Context

Wrap related content so TalkBack reads it as a single, meaningful unit.

```xml
<!-- ✅ Letter row as single focusable group -->
<LinearLayout
    android:focusable="true"
    android:clickable="true"
    android:contentDescription="Letter from Swiss Post, received today, unread"
    android:importantForAccessibility="yes">

    <ImageView android:importantForAccessibility="no" />  <!-- envelope icon -->
    <TextView android:importantForAccessibility="no" />   <!-- "Swiss Post" -->
    <TextView android:importantForAccessibility="no" />   <!-- "Today" -->
</LinearLayout>
```

---

## Custom Views

Custom views must override `onInitializeAccessibilityNodeInfo` to expose their role, state, and label to TalkBack.

### Custom Checkbox / Tri-State

```kotlin
class TriStateCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class State { UNCHECKED, CHECKED, INDETERMINATE }

    var state: State = State.UNCHECKED
        set(value) {
            field = value
            invalidate()
            // Notify accessibility service of state change
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
        }

    var label: String = ""

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = CheckBox::class.java.name  // TalkBack announces as "checkbox"
        info.isCheckable = true
        info.isChecked = state == State.CHECKED
        info.text = label
    }

    override fun performClick(): Boolean {
        cycleState()
        return super.performClick()
    }
}
```

### Custom Toggle / Segmented Control

```kotlin
class SegmentButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    var isSegmentSelected: Boolean = false
        set(value) {
            field = value
            refreshDrawableState()
        }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = RadioButton::class.java.name
        info.isChecked = isSegmentSelected
        info.isCheckable = true
    }
}
```

### Setting Description Programmatically

```kotlin
// ✅ Set at runtime when content is dynamic
iconButton.contentDescription = "Delete letter from ${letter.sender}"
iconButton.isImportantForAccessibility = true

// ✅ Suppress decorative view programmatically
decorativeView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
```

---

## RecyclerView & ViewHolder

The key rule: either the **row** handles accessibility OR its **children** do — not both. Having both the row and its children focusable causes TalkBack to read the same content twice, or read children out of context.

### Pattern A — Row-level description (most common)

Use when the row is tapped as a unit (e.g., opening a letter detail screen).

```kotlin
class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val senderText: TextView = itemView.findViewById(R.id.tv_sender)
    val subjectText: TextView = itemView.findViewById(R.id.tv_subject)
    val dateText: TextView = itemView.findViewById(R.id.tv_date)
    val menuButton: ImageButton = itemView.findViewById(R.id.btn_menu)
}

override fun onBindViewHolder(holder: LetterViewHolder, position: Int) {
    val letter = letters[position]

    // ✅ Row reads as one unit: "Letter from Swiss Post – Invoice – 3 Mar – unread"
    val unreadSuffix = if (letter.isUnread) ", unread" else ""
    holder.itemView.contentDescription =
        "Letter from ${letter.sender}, ${letter.subject}, ${letter.date}$unreadSuffix"

    // ✅ Children suppressed — row handles everything
    holder.senderText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    holder.subjectText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    holder.dateText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

    // ✅ Menu button stays individually accessible with contextual label
    holder.menuButton.contentDescription = "More options for letter from ${letter.sender}"
    holder.menuButton.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES

    // ✅ Row is interactive
    holder.itemView.isClickable = true
    holder.itemView.isFocusable = true
}
```

### Pattern B — Child-level description (when children are independently interactive)

Use when each child has its own action (e.g., a card with multiple buttons).

```kotlin
override fun onBindViewHolder(holder: ActionCardViewHolder, position: Int) {
    val item = items[position]

    // ✅ Container is NOT focusable — children handle themselves
    holder.itemView.importantForAccessibility =
        View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS  // suppress row
    // Re-enable individual children:
    holder.primaryButton.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    holder.secondaryButton.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES

    holder.primaryButton.contentDescription = "Open letter from ${item.sender}"
    holder.secondaryButton.contentDescription = "Delete letter from ${item.sender}"
}
```

---

## Accessibility Delegate

Use `ViewCompat.setAccessibilityDelegate` to add or override accessibility behaviour without subclassing. Ideal for third-party or legacy views.

### Add Role and State Description

```kotlin
// Give a custom view a meaningful role + dynamic state
ViewCompat.setAccessibilityDelegate(
    segmentButton,
    object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfoCompat
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.roleDescription = "Tab"  // TalkBack reads "Tab" before the label
            info.stateDescription = if (isSelected) "Selected" else ""
        }
    }
)
```

### Add Custom Actions (e.g. swipe-to-delete)

```kotlin
ViewCompat.setAccessibilityDelegate(
    letterRow,
    object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfoCompat
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            // TalkBack local context menu shows "Delete letter"
            info.addAction(
                AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_DISMISS,
                    "Delete letter"
                )
            )
        }

        override fun performAccessibilityAction(
            host: View,
            action: Int,
            args: Bundle?
        ): Boolean {
            if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS) {
                deleteLetter(host)
                return true
            }
            return super.performAccessibilityAction(host, action, args)
        }
    }
)
```

---

## Common Violations

| Violation | Root Cause | Fix |
|-----------|-----------|-----|
| Icon button announced as "Button" with no label | `contentDescription` missing on `ImageButton` | Set `android:contentDescription` in XML |
| Image read as resource file name | `contentDescription` not set on `ImageView` | Set `android:contentDescription` |
| Decorative image announced | `importantForAccessibility` not set | `android:importantForAccessibility="no"` |
| List item children read separately + redundantly | Both row and children are focusable | Suppress children with `importantForAccessibility="no"` |
| Form field read without its label | Missing `android:labelFor` | Link label `TextView` to `EditText` |
| Dynamic status update not announced | Missing `accessibilityLiveRegion` | `android:accessibilityLiveRegion="polite"` |
| Custom View not announced | `isFocusable` not set or `importantForAccessibility="no"` | Ensure `focusable="true"` and set `contentDescription` |
| Custom View role wrong ("View" instead of "Button") | Missing `onInitializeAccessibilityNodeInfo` override | Set `info.className = Button::class.java.name` |

---

## Related Documents

- [android-content-descriptions.md](./android-content-descriptions.md) — Compose contentDescription patterns
- [android-focus-semantics.md](./android-focus-semantics.md) — Compose semantics grouping and state
- [android-touch-targets.md](./android-touch-targets.md) — Touch target sizing (applies to Views and Compose)
- [android-headings.md](./android-headings.md) — Heading structure
