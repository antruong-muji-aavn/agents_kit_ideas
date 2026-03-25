---
name: web-semantic-html
description: Semantic HTML rules — text alternatives, native elements, landmark roles, skip links, heading hierarchy
---

# Semantic HTML Accessibility

## Purpose

Rules for using semantic HTML to provide accessible structure, text alternatives, and meaningful landmarks. Native elements are always preferred over ARIA workarounds.

## Table of Contents

- [Text Alternatives](#text-alternatives)
- [Native Elements Over ARIA](#native-elements-over-aria)
- [Landmark Roles](#landmark-roles)
- [Skip Links](#skip-links)
- [Heading Hierarchy](#heading-hierarchy)

## Related Documents

- [web-aria](./web-aria.md) — ARIA roles and attributes
- [web-keyboard-focus](./web-keyboard-focus.md) — Focus management
- [web-wcag-reference](./web-wcag-reference.md) — WCAG success criteria

---

## Text Alternatives

### Informative Images

Images that convey information require descriptive `alt` text. The description should convey the *purpose*, not appearance.

```html
<!-- ✅ Descriptive alt for informative image -->
<img src="chart.png" alt="Bar chart showing Q3 revenue up 12% year-over-year" />

<!-- ✅ Functional image (link/button) — describe the destination/action -->
<a href="/home">
  <img src="logo.svg" alt="Acme Corp — Home" />
</a>
```

```jsx
// React
<img src={chartSrc} alt="Q3 revenue chart showing 12% growth" />
```

### Decorative Images

Images that are purely visual should be hidden from screen readers.

```html
<!-- ✅ Decorative: empty alt + role="presentation" -->
<img src="divider.png" alt="" role="presentation" />

<!-- ✅ Inline SVG decorative -->
<svg aria-hidden="true" focusable="false">
  <use href="#icon-star" />
</svg>
```

```jsx
// React decorative icon
<img src="/decorative-swirl.png" alt="" role="presentation" />

// SVG icon used alongside text — hide icon from AT
<button>
  <StarIcon aria-hidden="true" focusable={false} />
  Add to favorites
</button>
```

### Complex Images

Charts, diagrams, and maps need extended descriptions.

```html
<!-- ✅ Short alt + long description via aria-describedby -->
<figure>
  <img
    src="org-chart.png"
    alt="Organization chart"
    aria-describedby="orgchart-desc"
  />
  <figcaption id="orgchart-desc">
    CEO at top, three VPs below: Engineering, Marketing, Sales.
    Each VP manages 4–6 directors.
  </figcaption>
</figure>
```

---

## Native Elements Over ARIA

The first rule of ARIA: use native HTML before reaching for ARIA. Native elements provide built-in keyboard support, focus management, and semantics.

### Buttons and Links

```html
<!-- ✅ Native button — keyboard accessible, fires on Enter/Space -->
<button type="button" onclick="doAction()">Save Draft</button>

<!-- ❌ Avoid — requires manual keyboard handling and role -->
<div role="button" tabindex="0" onclick="doAction()" onkeydown="...">
  Save Draft
</div>

<!-- ✅ Link — navigates to a URL -->
<a href="/profile">View Profile</a>

<!-- ❌ Avoid — loses href semantics, bookmark, open-in-new-tab -->
<span role="link" tabindex="0" onclick="navigate('/profile')">
  View Profile
</span>
```

### Form Controls

```html
<!-- ✅ Native checkbox -->
<input type="checkbox" id="remember" name="remember" />
<label for="remember">Remember me</label>

<!-- ❌ Avoid — complex ARIA required for equivalent behaviour -->
<div role="checkbox" aria-checked="false" tabindex="0">Remember me</div>

<!-- ✅ Native select -->
<select id="country" name="country">
  <option value="us">United States</option>
  <option value="ca">Canada</option>
</select>
```

### Interactive Disclosure

```html
<!-- ✅ Native details/summary — free keyboard + screen reader support -->
<details>
  <summary>Shipping information</summary>
  <p>Orders ship within 2–3 business days.</p>
</details>
```

---

## Landmark Roles

Landmarks let screen reader users jump between page regions. Use one `<main>` per page. Label duplicate landmarks (multiple `<nav>`) with `aria-label`.

```html
<body>
  <!-- Page header -->
  <header>
    <a href="/">Acme Corp</a>
    <nav aria-label="Primary navigation">
      <ul>
        <li><a href="/products">Products</a></li>
        <li><a href="/about">About</a></li>
      </ul>
    </nav>
  </header>

  <!-- Main content area — one per page -->
  <main id="main-content">
    <h1>Products</h1>
    <!-- page content -->
  </main>

  <!-- Supplementary content -->
  <aside aria-label="Related articles">
    <h2>You might also like</h2>
    <!-- sidebar -->
  </aside>

  <!-- Page footer -->
  <footer>
    <nav aria-label="Footer navigation">
      <a href="/legal">Legal</a>
    </nav>
  </footer>
</body>
```

```jsx
// React with semantic elements
export default function Layout({ children }) {
  return (
    <>
      <header>
        <nav aria-label="Primary navigation">{/* nav links */}</nav>
      </header>
      <main id="main-content">{children}</main>
      <footer>{/* footer content */}</footer>
    </>
  );
}
```

### Landmark Quick Reference

| Element | Implicit Role | Notes |
|---------|--------------|-------|
| `<header>` | `banner` | Top-level only; inside `<article>` it has no role |
| `<nav>` | `navigation` | Label multiples with `aria-label` |
| `<main>` | `main` | One per page |
| `<aside>` | `complementary` | Label if content isn't obvious |
| `<footer>` | `contentinfo` | Top-level only |
| `<section>` | `region` | Only creates landmark when it has `aria-label` or `aria-labelledby` |
| `<article>` | `article` | Self-contained content |
| `<form>` | `form` | Creates landmark when named |
| `<search>` | `search` | HTML5.3 — use for search widgets |

---

## Skip Links

Skip links allow keyboard users to bypass repeated navigation and jump directly to main content.

```html
<!-- Place as first focusable element in <body> -->
<a href="#main-content" class="skip-link">Skip to main content</a>

<header>
  <!-- navigation -->
</header>

<main id="main-content" tabindex="-1">
  <!-- main content -->
</main>
```

```css
/* Visible only on focus — hidden off-screen at rest */
.skip-link {
  position: absolute;
  top: -9999px;
  left: 0;
  z-index: 9999;
  background: #005fcc;
  color: #fff;
  padding: 8px 16px;
  text-decoration: none;
  border-radius: 0 0 4px 4px;
}

.skip-link:focus {
  top: 0;
}
```

---

## Heading Hierarchy

Headings communicate page structure. Screen reader users navigate by heading level. Never skip levels for visual styling.

### Rules

- One `<h1>` per page — the page title
- Levels increase by one (`h1 → h2 → h3`) — never skip
- Use CSS for visual size, not heading level
- Dynamic content additions should include an appropriate heading

```html
<!-- ✅ Correct hierarchy -->
<h1>Account Settings</h1>
  <h2>Profile</h2>
    <h3>Personal Information</h3>
    <h3>Contact Details</h3>
  <h2>Security</h2>
    <h3>Password</h3>
    <h3>Two-Factor Authentication</h3>

<!-- ❌ Skipped level — jumps from h1 to h3 -->
<h1>Account Settings</h1>
  <h3>Profile</h3>  <!-- should be h2 -->
```

```jsx
// React — heading level as prop for reuse
function SectionHeading({ level = 2, children, ...props }) {
  const Tag = `h${level}`;
  return <Tag {...props}>{children}</Tag>;
}

// Usage
<SectionHeading level={2}>Profile</SectionHeading>
<SectionHeading level={3}>Personal Information</SectionHeading>
```

### Visually Styled vs Semantic Level

```html
<!-- ✅ Semantic h2 styled to look like h4 -->
<h2 class="text-sm font-medium text-gray-500">Profile</h2>

<!-- ❌ Visual h4 used because it "looks right" — breaks hierarchy -->
<h4>Profile</h4>
```

### Checklist

- [ ] Single `<h1>` per page matching the page title
- [ ] No skipped heading levels
- [ ] Headings describe their section content
- [ ] All headings are actual `<h1>`–`<h6>` elements, not styled `<div>`s
- [ ] Dynamic content regions include appropriate headings
