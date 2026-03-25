---
name: web-contrast
description: Colour contrast ratios, forced-colors, prefers-color-scheme, prefers-reduced-motion, visually-hidden pattern
---

# Colour & Visual Accessibility

## Purpose

Rules for colour contrast, user preference media queries, visually-hidden patterns, and non-colour-dependent communication. Visual presentation must not be the sole means of conveying information.

## Table of Contents

- [Contrast Ratios](#contrast-ratios)
- [Non-Colour Information](#non-colour-information)
- [User Preference Media Queries](#user-preference-media-queries)
- [Visually-Hidden Pattern](#visually-hidden-pattern)
- [Common Violations](#common-violations)
- [Testing Tools](#testing-tools)

## Related Documents

- [web-forms](./web-forms.md) — Error state colour requirements
- [web-keyboard-focus](./web-keyboard-focus.md) — Focus ring contrast
- [web-wcag-reference](./web-wcag-reference.md) — SC 1.4.3, 1.4.6, 1.4.11, 1.4.12

---

## Contrast Ratios

### WCAG AA Requirements

| Content Type | Minimum Ratio | Applies To |
|-------------|--------------|-----------|
| Normal text (< 18pt / < 14pt bold) | **4.5:1** | All text |
| Large text (≥ 18pt / ≥ 14pt bold) | **3:1** | Headings, large UI text |
| UI components (borders, icons) | **3:1** | Button borders, input borders, icons |
| Focus indicators | **3:1** | Focus ring vs adjacent background |
| Disabled elements | **exempt** | Disabled inputs, buttons |
| Decorative elements | **exempt** | Purely decorative images, backgrounds |

**18pt ≈ 24px, 14pt bold ≈ 18.67px bold**

### CSS Colour Examples

```css
/* ✅ Normal text — 4.5:1+ on white (#ffffff) */
body { color: #374151; }          /* #374151 on white = 8.6:1 ✅ */
.label { color: #1f2937; }        /* #1f2937 on white = 12.6:1 ✅ */

/* ❌ Common failures */
.placeholder { color: #9ca3af; }  /* #9ca3af on white = 2.5:1 ❌ */
.muted { color: #d1d5db; }        /* #d1d5db on white = 1.6:1 ❌ */
.secondary-text { color: #6b7280; } /* #6b7280 on white = 4.6:1 ✅ (barely) */

/* ✅ UI components — 3:1+ */
input { border: 1px solid #6b7280; } /* #6b7280 on white = 4.6:1 ✅ */
button { border: 1px solid #374151; }
```

### Contrast on Coloured Backgrounds

```css
/* ✅ Check foreground vs background (not vs white) */
.badge-warning {
  background-color: #fef3c7; /* amber-100 */
  color: #92400e;            /* amber-800 — 7.3:1 on #fef3c7 ✅ */
}

.badge-info {
  background-color: #dbeafe; /* blue-100 */
  color: #1e40af;            /* blue-800 — 7.4:1 on #dbeafe ✅ */
}

/* ❌ Low contrast badge */
.badge-error {
  background-color: #fee2e2; /* red-100 */
  color: #f87171;            /* red-400 — 1.8:1 ❌ */
}
```

---

## Non-Colour Information

Colour must never be the only visual means of conveying information.

```html
<!-- ❌ Colour-only error state -->
<input type="email" style="border-color: red;" />

<!-- ✅ Colour + icon + text -->
<input
  type="email"
  class="input-error"
  aria-invalid="true"
  aria-describedby="email-error"
/>
<p id="email-error" class="error-msg">
  <span aria-hidden="true">⚠ </span>Please enter a valid email address.
</p>
```

```css
/* ✅ Error state — border colour + thicker border + icon */
.input-error {
  border-color: #b91c1c;
  border-width: 2px;
  background-image: url('/icons/error.svg');
  background-position: right 10px center;
  background-repeat: no-repeat;
  padding-right: 36px;
}

/* ✅ Required field — asterisk + sr-only text, not just red */
.required-star {
  color: #b91c1c;
  font-weight: bold;
}
```

### Charts and Data Visualisations

```html
<!-- ✅ Chart: colour + pattern + label -->
<svg aria-labelledby="chart-title chart-desc">
  <title id="chart-title">Monthly revenue Q1 2025</title>
  <desc id="chart-desc">
    Jan: $12k, Feb: $15k, Mar: $18k. Trend line shows 25% growth.
  </desc>
  <!-- bars use both colour and hatching patterns -->
</svg>
```

---

## User Preference Media Queries

### prefers-color-scheme

```css
/* ✅ Light mode defaults, dark mode overrides */
:root {
  --color-bg: #ffffff;
  --color-text: #111827;
  --color-border: #d1d5db;
  --color-focus: #005fcc;
}

@media (prefers-color-scheme: dark) {
  :root {
    --color-bg: #111827;
    --color-text: #f9fafb;
    --color-border: #374151;
    --color-focus: #60a5fa;
  }
}

body {
  background-color: var(--color-bg);
  color: var(--color-text);
}
```

```jsx
// React — detect colour scheme preference
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

// Or with useState for dynamic changes
function useColorScheme() {
  const [scheme, setScheme] = useState(
    window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  );

  useEffect(() => {
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = (e) => setScheme(e.matches ? 'dark' : 'light');
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);

  return scheme;
}
```

### forced-colors (High Contrast Mode)

Windows High Contrast Mode replaces colours with system colours. Never hard-code colours on interactive elements' borders.

```css
/* ✅ Preserve focus ring in forced-colors mode */
:focus-visible {
  outline: 2px solid transparent; /* invisible in normal mode... */
  box-shadow: 0 0 0 3px #005fcc;  /* ...but box-shadow used in normal */
}

@media (forced-colors: active) {
  /* outline: transparent becomes visible using system highlight colour */
  :focus-visible {
    outline: 2px solid ButtonText;
    box-shadow: none;
  }

  /* Restore button borders that CSS hid */
  .btn {
    border: 2px solid ButtonText;
  }
}
```

### prefers-reduced-motion

```css
/* ✅ Disable non-essential animation for motion-sensitive users */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}

/* Or target specific animations */
.spinner {
  animation: spin 1s linear infinite;
}

@media (prefers-reduced-motion: reduce) {
  .spinner {
    animation: none;
    /* Show static loading indicator instead */
    border-top-color: currentColor;
  }
}
```

```jsx
// React — motion-safe animations
const prefersReducedMotion =
  typeof window !== 'undefined'
    ? window.matchMedia('(prefers-reduced-motion: reduce)').matches
    : false;

function AnimatedComponent() {
  return (
    <motion.div
      animate={prefersReducedMotion ? {} : { x: 100 }}
      transition={prefersReducedMotion ? { duration: 0 } : { duration: 0.3 }}
    />
  );
}
```

---

## Visually-Hidden Pattern

Use to provide screen-reader-only text without visually displaying it.

```css
/* ✅ Standard visually-hidden / sr-only class */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

/* ✅ Make focusable when focused (for skip links) */
.sr-only-focusable:focus {
  position: static;
  width: auto;
  height: auto;
  margin: 0;
  overflow: visible;
  clip: auto;
  white-space: normal;
}
```

```jsx
// Tailwind: className="sr-only"
// Custom: className={styles.srOnly}

// Usage
<button>
  <StarIcon aria-hidden="true" />
  <span className="sr-only">Add Blue Widget to favourites</span>
</button>
```

**Do NOT use:** `display: none`, `visibility: hidden`, `opacity: 0`, `height: 0` — these hide content from all users including screen readers.

---

## Common Violations

| Violation | WCAG | Fix |
|-----------|------|-----|
| Light gray placeholder text | 1.4.3 | Use text ≥ 4.5:1 or add floating label |
| Placeholder as only label | 1.3.1 | Add visible `<label>` element |
| Low-contrast focus ring | 1.4.11 | Focus ring ≥ 3:1 vs adjacent background |
| Red-only error border | 1.4.1 | Add icon, text, or border-width change |
| Pale disabled text (< 3:1) | exempt | Exempt, but aim for legibility |
| Animation without reduced-motion | 2.3.3 | Wrap in `@media (prefers-reduced-motion)` |
| Missing dark mode contrast check | 1.4.3 | Verify both light and dark mode ratios |

---

## Testing Tools

| Tool | How to Access |
|------|--------------|
| Chrome DevTools Contrast Checker | Elements → Styles → click colour swatch |
| Lighthouse Accessibility Audit | DevTools → Lighthouse → Accessibility |
| axe DevTools Browser Extension | Installed extension → Run axe |
| WebAIM Contrast Checker | contrast-checker.webaim.org |
| Colour Oracle (simulation) | colororacle.org — simulates colour blindness |
| Windows High Contrast Mode | Settings → Ease of Access → High Contrast |

### Checklist

- [ ] All normal text ≥ 4.5:1 contrast ratio
- [ ] All large text (≥ 24px / ≥ 18.67px bold) ≥ 3:1
- [ ] All UI components (borders, icons) ≥ 3:1
- [ ] Focus rings ≥ 3:1 against adjacent background
- [ ] Error states use colour + additional indicator (icon, text, border)
- [ ] `prefers-color-scheme` dark mode colours verified
- [ ] `forced-colors: active` does not break focus or borders
- [ ] `prefers-reduced-motion` disables non-essential animations
- [ ] Charts/graphs do not rely on colour alone to convey data
