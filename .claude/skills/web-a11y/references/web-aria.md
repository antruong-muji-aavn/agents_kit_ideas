---
name: web-aria
description: ARIA labels, roles, live regions, dialog/tab/progressbar patterns, state attributes
---

# ARIA Accessibility

## Purpose

Rules for ARIA (Accessible Rich Internet Applications) usage — when to apply labels, roles, states, and live regions for widgets not covered by native HTML semantics.

## Table of Contents

- [ARIA Labels](#aria-labels)
- [ARIA Roles](#aria-roles)
- [Live Regions](#live-regions)
- [State Attributes](#state-attributes)
- [Modal Dialog Pattern](#modal-dialog-pattern)
- [Tab Widget Pattern](#tab-widget-pattern)

## Related Documents

- [web-semantic-html](./web-semantic-html.md) — Native elements first
- [web-keyboard-focus](./web-keyboard-focus.md) — Keyboard and focus management
- [web-forms](./web-forms.md) — Form-specific ARIA patterns

---

## ARIA Labels

### aria-label

Use when there is no visible text label for the element.

```html
<!-- ✅ Icon-only button -->
<button aria-label="Close dialog">
  <svg aria-hidden="true" focusable="false"><!-- × icon --></svg>
</button>

<!-- ✅ Landmark with duplicate type -->
<nav aria-label="Primary navigation">...</nav>
<nav aria-label="Breadcrumb">...</nav>

<!-- ❌ Redundant — button text already labels it -->
<button aria-label="Save button">Save</button>
```

### aria-labelledby

Use to associate an element with an existing visible label in the DOM.

```html
<!-- ✅ Dialog labeled by its heading -->
<div role="dialog" aria-labelledby="dialog-title" aria-modal="true">
  <h2 id="dialog-title">Confirm Deletion</h2>
  <p>Are you sure you want to delete this item?</p>
  <button>Delete</button>
  <button>Cancel</button>
</div>

<!-- ✅ Multiple label sources (concatenated by AT) -->
<span id="qty-label">Quantity</span>
<span id="product-name">Blue Widget</span>
<input
  type="number"
  aria-labelledby="qty-label product-name"
/>
<!-- Screen reader: "Quantity Blue Widget" -->
```

### aria-describedby

Provides supplementary description — announced after the label.

```html
<!-- ✅ Input with description -->
<label for="password">Password</label>
<input
  id="password"
  type="password"
  aria-describedby="password-hint"
/>
<p id="password-hint">
  Must be at least 8 characters and include a number.
</p>

<!-- ✅ Error message linked to input -->
<input
  id="email"
  type="email"
  aria-invalid="true"
  aria-describedby="email-error"
/>
<p id="email-error" role="alert">
  Please enter a valid email address.
</p>
```

---

## ARIA Roles

### Common Widget Roles

```html
<!-- Alert — immediate, time-sensitive announcement -->
<div role="alert">
  Your session will expire in 5 minutes.
</div>

<!-- Status — polite status update -->
<div role="status" aria-live="polite">
  3 results found.
</div>

<!-- Progressbar -->
<div
  role="progressbar"
  aria-valuenow="65"
  aria-valuemin="0"
  aria-valuemax="100"
  aria-valuetext="65% complete"
>
  <div style="width: 65%"></div>
</div>
```

```jsx
// React progress bar component
function ProgressBar({ value, max = 100, label }) {
  const percent = Math.round((value / max) * 100);
  return (
    <div
      role="progressbar"
      aria-valuenow={value}
      aria-valuemin={0}
      aria-valuemax={max}
      aria-valuetext={`${percent}% complete`}
      aria-label={label}
    >
      <div style={{ width: `${percent}%` }} />
    </div>
  );
}
```

### Composite Widget Roles

```html
<!-- Listbox (single or multi-select) -->
<ul role="listbox" aria-label="Choose a color" aria-multiselectable="false">
  <li role="option" aria-selected="true" id="opt-red">Red</li>
  <li role="option" aria-selected="false" id="opt-blue">Blue</li>
  <li role="option" aria-selected="false" id="opt-green">Green</li>
</ul>

<!-- Menu / Menuitem -->
<ul role="menu" aria-label="File actions">
  <li role="menuitem">New File</li>
  <li role="menuitem">Open</li>
  <li role="separator"></li>
  <li role="menuitem" aria-disabled="true">Save</li>
</ul>
```

---

## Live Regions

Live regions announce dynamic content changes to screen reader users without moving focus.

### aria-live Values

| Value | Behaviour |
|-------|-----------|
| `polite` | Waits for user to finish current interaction before announcing |
| `assertive` | Interrupts immediately — use sparingly for urgent updates |
| `off` (default) | No announcements |

```html
<!-- ✅ Search results count — polite update -->
<div aria-live="polite" aria-atomic="true">
  <span id="result-count"></span>
</div>

<!-- ✅ Critical error or session expiry — assertive -->
<div role="alert">
  <!-- role="alert" is equivalent to aria-live="assertive" aria-atomic="true" -->
  Your changes could not be saved. Please try again.
</div>
```

### aria-atomic

Controls whether the full region or only the changed nodes are announced.

```html
<!-- ✅ Announce entire region on any change -->
<div aria-live="polite" aria-atomic="true">
  Step <span id="current-step">2</span> of <span id="total-steps">5</span>
</div>
<!-- Announces: "Step 2 of 5" (not just "2") -->
```

### React Live Region Pattern

```jsx
// StatusAnnouncer — inject into <body> once, update text to trigger announcement
function StatusAnnouncer({ message, priority = 'polite' }) {
  return (
    <div
      aria-live={priority}
      aria-atomic="true"
      style={{
        position: 'absolute',
        width: 1,
        height: 1,
        overflow: 'hidden',
        clip: 'rect(0 0 0 0)',
        whiteSpace: 'nowrap',
      }}
    >
      {message}
    </div>
  );
}
```

---

## State Attributes

Use ARIA state attributes to reflect dynamic widget states that native HTML cannot express.

```html
<!-- Expanded/collapsed disclosure -->
<button aria-expanded="false" aria-controls="faq-1-answer">
  What is your return policy?
</button>
<div id="faq-1-answer" hidden>
  We accept returns within 30 days.
</div>

<!-- Selected tab / tree node -->
<button role="tab" aria-selected="true" aria-controls="panel-1">Overview</button>
<button role="tab" aria-selected="false" aria-controls="panel-2">Details</button>

<!-- Checked state for custom checkbox -->
<div
  role="checkbox"
  aria-checked="mixed"
  tabindex="0"
>
  Select all
</div>

<!-- Disabled state -->
<button aria-disabled="true">Submit</button>
```

```jsx
// React — controlled aria-expanded
function Disclosure({ summary, children }) {
  const [open, setOpen] = useState(false);
  const panelId = useId();

  return (
    <>
      <button
        aria-expanded={open}
        aria-controls={panelId}
        onClick={() => setOpen((o) => !o)}
      >
        {summary}
      </button>
      <div id={panelId} hidden={!open}>
        {children}
      </div>
    </>
  );
}
```

---

## Modal Dialog Pattern

Modal dialogs require focus trapping, labeling, and keyboard dismissal.

```html
<!-- ✅ Full dialog pattern -->
<div
  role="dialog"
  aria-modal="true"
  aria-labelledby="modal-title"
  aria-describedby="modal-desc"
  id="confirm-modal"
>
  <h2 id="modal-title">Delete Account</h2>
  <p id="modal-desc">
    This action is permanent and cannot be undone.
  </p>
  <button id="modal-confirm">Delete</button>
  <button id="modal-cancel">Cancel</button>
</div>
```

```js
// Focus management on open/close
function openModal(modal) {
  modal.removeAttribute('hidden');
  // Move focus to first focusable element or the dialog itself
  const firstFocusable = modal.querySelector('button, [href], input');
  firstFocusable?.focus();
  // Trap focus — see web-keyboard-focus.md for full implementation
}

function closeModal(modal, triggerEl) {
  modal.setAttribute('hidden', '');
  // Return focus to the element that opened the modal
  triggerEl?.focus();
}
```

```jsx
// React dialog with useRef focus management
function Modal({ isOpen, onClose, title, children }) {
  const dialogRef = useRef(null);
  const closeBtnRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      closeBtnRef.current?.focus();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      ref={dialogRef}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-heading"
    >
      <h2 id="modal-heading">{title}</h2>
      {children}
      <button ref={closeBtnRef} onClick={onClose}>
        Close
      </button>
    </div>
  );
}
```

---

## Tab Widget Pattern

```html
<!-- ✅ Tab list -->
<div>
  <ul role="tablist" aria-label="Product information">
    <li>
      <button
        role="tab"
        id="tab-overview"
        aria-selected="true"
        aria-controls="panel-overview"
      >
        Overview
      </button>
    </li>
    <li>
      <button
        role="tab"
        id="tab-specs"
        aria-selected="false"
        aria-controls="panel-specs"
        tabindex="-1"
      >
        Specifications
      </button>
    </li>
  </ul>

  <div
    role="tabpanel"
    id="panel-overview"
    aria-labelledby="tab-overview"
  >
    <p>Product overview content...</p>
  </div>

  <div
    role="tabpanel"
    id="panel-specs"
    aria-labelledby="tab-specs"
    hidden
  >
    <p>Technical specifications...</p>
  </div>
</div>
```

**Keyboard pattern:** Arrow keys move between tabs (activate-on-arrow or manual), Enter/Space activate tab in manual mode, Home/End jump to first/last tab, Tab moves into the active panel.

### Checklist

- [ ] All interactive ARIA elements have accessible names
- [ ] `aria-live` regions exist in DOM before content is injected
- [ ] Dialog has `aria-modal="true"` and is labeled
- [ ] Tabs use `role="tablist"`, `role="tab"`, `role="tabpanel"`
- [ ] State attributes (`aria-expanded`, `aria-selected`) reflect current state
- [ ] `aria-hidden="true"` on decorative SVGs and icons
