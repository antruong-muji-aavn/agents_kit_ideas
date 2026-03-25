---
name: web-keyboard-focus
description: Keyboard navigation rules — :focus-visible, focus trapping, tabIndex, keyboard events, focus order
---

# Keyboard & Focus Accessibility

## Purpose

Rules for keyboard navigation, focus styles, focus trapping in modals, and programmatic focus management. All interactive functionality must be operable without a mouse.

## Table of Contents

- [Focus Styles](#focus-styles)
- [Tab Order and tabIndex](#tab-order-and-tabindex)
- [Keyboard Event Handlers](#keyboard-event-handlers)
- [Focus Trapping](#focus-trapping)
- [Programmatic Focus Management](#programmatic-focus-management)
- [Focus Order](#focus-order)

## Related Documents

- [web-aria](./web-aria.md) — ARIA states (aria-expanded, aria-controls)
- [web-semantic-html](./web-semantic-html.md) — Native elements with built-in keyboard support
- [web-contrast](./web-contrast.md) — Focus ring contrast requirements

---

## Focus Styles

### Never Remove :focus Without a Replacement

Removing the focus outline breaks keyboard navigation for sighted keyboard users.

```css
/* ❌ Never do this */
* { outline: none; }
button:focus { outline: none; }

/* ✅ Replace with a styled :focus-visible indicator */
:focus-visible {
  outline: 2px solid #005fcc;
  outline-offset: 2px;
  border-radius: 2px;
}

/* ✅ Hide outline for mouse/touch, keep for keyboard */
:focus:not(:focus-visible) {
  outline: none;
}
```

### :focus-visible vs :focus

`:focus-visible` applies focus styles only when the browser determines keyboard navigation is active. Mouse clicks on buttons do not trigger it; Tab key presses do.

```css
/* ✅ Best practice — keyboard-only focus ring */
.btn:focus-visible {
  outline: 3px solid #005fcc;
  outline-offset: 3px;
}

/* ✅ High-contrast focus ring that works on any background */
:focus-visible {
  outline: 2px solid transparent;
  box-shadow: 0 0 0 3px #005fcc;
}
```

### Contrast Requirements

Focus indicators must meet WCAG 2.1 SC 1.4.11 (Non-text Contrast):

- Focus indicator must have at least **3:1** contrast ratio against adjacent colours
- WCAG 2.2 SC 2.4.11 adds: focus indicator must be at least 2 CSS pixels wide

```css
/* ✅ High-contrast focus ring */
:focus-visible {
  outline: 3px solid #005fcc; /* #005fcc on white = 7.3:1 */
  outline-offset: 2px;
}

/* ✅ Visible on dark backgrounds too */
:focus-visible {
  outline: 2px solid transparent;
  box-shadow:
    0 0 0 2px #ffffff,  /* white halo */
    0 0 0 4px #005fcc;  /* blue ring */
}
```

---

## Tab Order and tabIndex

### tabIndex Values

| Value | Behaviour | When to Use |
|-------|-----------|-------------|
| `tabindex="0"` | Element joins natural document flow tab order | Custom interactive widgets |
| `tabindex="-1"` | Focusable via JS only, not in tab order | Programmatic focus targets (modal dialogs, skip-link destinations) |
| `tabindex="N"` (positive) | Creates explicit order — **avoid** | Never — breaks predictable tab order |

```html
<!-- ✅ Custom widget in tab order -->
<div role="slider" tabindex="0" aria-valuenow="50" aria-valuemin="0" aria-valuemax="100">
  Volume
</div>

<!-- ✅ Programmatic focus target — not in natural tab flow -->
<main id="main-content" tabindex="-1">
  <!-- skip link target -->
</main>

<!-- ❌ Never use positive tabindex -->
<button tabindex="3">Submit</button>
```

### Interactive vs Non-Interactive

```html
<!-- ✅ Only interactive elements in tab order -->
<p>Read this paragraph.</p>          <!-- no tabindex -->
<img src="photo.jpg" alt="..." />    <!-- no tabindex -->
<button>Click me</button>            <!-- naturally focusable -->

<!-- ❌ Non-interactive element forced into tab order -->
<p tabindex="0">Read this paragraph.</p>
```

---

## Keyboard Event Handlers

All mouse interactions must have keyboard equivalents.

### Button-Like Elements

Native `<button>` elements fire `click` on both Enter and Space. Custom elements must replicate this.

```js
// ✅ Native button — keyboard handling is automatic
document.querySelector('button').addEventListener('click', handleAction);

// ✅ Custom button widget — handle keyboard events manually
customButton.addEventListener('keydown', (e) => {
  if (e.key === 'Enter' || e.key === ' ') {
    e.preventDefault(); // Prevent page scroll on Space
    handleAction();
  }
});
```

### Arrow Key Navigation for Composite Widgets

Widgets like tabs, menus, listboxes use arrow keys to move within the widget (roving tabindex pattern).

```js
// ✅ Roving tabindex for a toolbar
const items = Array.from(toolbar.querySelectorAll('[role="button"]'));
let currentIndex = 0;

toolbar.addEventListener('keydown', (e) => {
  let newIndex = currentIndex;

  if (e.key === 'ArrowRight') {
    newIndex = (currentIndex + 1) % items.length;
  } else if (e.key === 'ArrowLeft') {
    newIndex = (currentIndex - 1 + items.length) % items.length;
  } else if (e.key === 'Home') {
    newIndex = 0;
  } else if (e.key === 'End') {
    newIndex = items.length - 1;
  } else {
    return;
  }

  e.preventDefault();
  items[currentIndex].tabIndex = -1;
  items[newIndex].tabIndex = 0;
  items[newIndex].focus();
  currentIndex = newIndex;
});
```

```jsx
// React — keyboard handler for custom listbox
function Listbox({ options, value, onChange }) {
  const [activeIndex, setActiveIndex] = useState(0);

  const handleKeyDown = (e) => {
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setActiveIndex((i) => Math.min(i + 1, options.length - 1));
        break;
      case 'ArrowUp':
        e.preventDefault();
        setActiveIndex((i) => Math.max(i - 1, 0));
        break;
      case 'Enter':
      case ' ':
        e.preventDefault();
        onChange(options[activeIndex]);
        break;
      case 'Home':
        setActiveIndex(0);
        break;
      case 'End':
        setActiveIndex(options.length - 1);
        break;
    }
  };

  return (
    <ul role="listbox" onKeyDown={handleKeyDown} tabIndex={0}>
      {options.map((opt, i) => (
        <li
          key={opt.value}
          role="option"
          aria-selected={opt.value === value}
          id={`opt-${opt.value}`}
        >
          {opt.label}
        </li>
      ))}
    </ul>
  );
}
```

### Escape Key

Escape should dismiss overlays, modals, popovers, and dropdowns.

```js
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape' && modal.isOpen) {
    closeModal();
  }
});
```

---

## Focus Trapping

When a modal dialog is open, keyboard focus must stay within it.

```js
// ✅ Focus trap implementation
function trapFocus(container) {
  const focusable = container.querySelectorAll(
    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
  );
  const firstEl = focusable[0];
  const lastEl = focusable[focusable.length - 1];

  container.addEventListener('keydown', function trap(e) {
    if (e.key !== 'Tab') return;

    if (e.shiftKey) {
      // Shift+Tab
      if (document.activeElement === firstEl) {
        e.preventDefault();
        lastEl.focus();
      }
    } else {
      // Tab
      if (document.activeElement === lastEl) {
        e.preventDefault();
        firstEl.focus();
      }
    }
  });
}
```

```jsx
// React — useFocusTrap hook
function useFocusTrap(ref, isActive) {
  useEffect(() => {
    if (!isActive || !ref.current) return;

    const el = ref.current;
    const focusableSelectors =
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])';
    const focusable = Array.from(el.querySelectorAll(focusableSelectors));
    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    function handleTab(e) {
      if (e.key !== 'Tab') return;
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last?.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first?.focus();
      }
    }

    el.addEventListener('keydown', handleTab);
    first?.focus();

    return () => el.removeEventListener('keydown', handleTab);
  }, [isActive, ref]);
}
```

---

## Programmatic Focus Management

Move focus intentionally when UI state changes significantly.

```js
// ✅ Return focus to trigger after modal closes
let modalTrigger;

openBtn.addEventListener('click', () => {
  modalTrigger = openBtn;
  openModal();
});

function closeModal() {
  modal.setAttribute('hidden', '');
  modalTrigger?.focus(); // ← return focus
}

// ✅ Move focus to heading after route change (SPA)
function onRouteChange() {
  const heading = document.querySelector('h1');
  if (heading) {
    heading.setAttribute('tabindex', '-1');
    heading.focus();
  }
}

// ✅ Announce inline error and focus the first invalid field
function onFormError(firstInvalidField) {
  firstInvalidField.focus();
}
```

---

## Focus Order

Focus order must match the visual reading order (left-to-right, top-to-bottom in LTR layouts).

```html
<!-- ✅ DOM order matches visual order -->
<header>...</header>
<nav>...</nav>
<main>
  <article>...</article>
  <aside>...</aside>
</main>
<footer>...</footer>

<!-- ❌ CSS changes visual order but DOM order stays wrong -->
<style>
  .sidebar { order: -1; } /* CSS Grid/Flexbox order */
</style>
<div class="layout">
  <main>Content first in DOM...</main>
  <div class="sidebar">...but sidebar first visually</div>
</div>
```

When using CSS `order`, `flex-direction: row-reverse`, or `grid-template-areas` to change visual order, verify focus still follows visual order or restructure the DOM instead.

### Checklist

- [ ] `:focus-visible` styles are present and meet 3:1 contrast
- [ ] `:focus { outline: none }` never applied without replacement
- [ ] No positive `tabindex` values in codebase
- [ ] Custom interactive widgets handle Enter/Space keyboard events
- [ ] Composite widgets use arrow keys internally (roving tabindex)
- [ ] Modals trap focus and return focus to trigger on close
- [ ] Escape key dismisses modals, drawers, and menus
- [ ] Focus order matches visual reading order
- [ ] SPA route changes move focus to new page heading or main
