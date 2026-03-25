---
name: web-wcag-reference
description: WCAG 2.1 success criteria quick reference, common ARIA patterns, screen reader commands, and audit checklist
user-invocable: false
---

# WCAG 2.1 Quick Reference & Patterns

## Success Criteria — Level A (Must Pass)

| SC | Name | Key Requirement |
|----|------|----------------|
| 1.1.1 | Non-text Content | All `<img>` have meaningful `alt` or `alt=""` for decorative |
| 1.2.1 | Audio-only and Video-only | Provide text transcript |
| 1.2.2 | Captions (Prerecorded) | Synchronized captions for video |
| 1.2.3 | Audio Description | Audio description or text alternative for video |
| 1.3.1 | Info and Relationships | Use semantic HTML (`<nav>`, `<main>`, `<header>`, `<h1>`–`<h6>`) |
| 1.3.2 | Meaningful Sequence | DOM order matches visual order |
| 1.4.1 | Use of Color | Don't convey info by color alone |
| 1.4.2 | Audio Control | Mechanism to pause/stop auto-playing audio |
| 2.1.1 | Keyboard | All functionality operable via keyboard |
| 2.1.2 | No Keyboard Trap | Focus can always move away from any component |
| 2.4.1 | Bypass Blocks | Skip navigation link or landmark roles |
| 2.4.2 | Page Titled | Descriptive `<title>` on every page |
| 2.4.3 | Focus Order | Tab order matches logical reading order |
| 2.4.4 | Link Purpose (In Context) | Link text identifies purpose |
| 3.1.1 | Language of Page | `<html lang="en">` |
| 3.2.1 | On Focus | No context change on focus |
| 3.2.2 | On Input | No context change on input without warning |
| 3.3.1 | Error Identification | Errors described in text (not color alone) |
| 3.3.2 | Labels or Instructions | Input fields have visible labels |
| 4.1.1 | Parsing | Valid HTML, no duplicate IDs |
| 4.1.2 | Name, Role, Value | All interactive elements have accessible name + role |

## Success Criteria — Level AA (Our Target)

| SC | Name | Key Requirement |
|----|------|----------------|
| 1.2.4 | Captions (Live) | Real-time captions for live video |
| 1.2.5 | Audio Description (Prerecorded) | Audio description for prerecorded video |
| 1.3.4 | Orientation | Content not locked to portrait/landscape |
| 1.3.5 | Identify Input Purpose | Input purpose programmatically determinable (`autocomplete`) |
| 1.4.3 | Contrast (Minimum) | 4.5:1 normal text, 3:1 large text (18pt+ or 14pt+ bold) |
| 1.4.4 | Resize Text | Text resizable to 200% without loss |
| 1.4.5 | Images of Text | Avoid text in images |
| 1.4.10 | Reflow | Content reflows at 320px width without horizontal scroll |
| 1.4.11 | Non-text Contrast | 3:1 for UI components and graphical objects |
| 1.4.12 | Text Spacing | Content readable with increased letter/word/line spacing |
| 1.4.13 | Content on Hover/Focus | Dismissible, hoverable, persistent popups/tooltips |
| 2.4.5 | Multiple Ways | More than one way to locate a page |
| 2.4.6 | Headings and Labels | Descriptive headings and labels |
| 2.4.7 | Focus Visible | Keyboard focus indicator always visible |
| 2.5.1 | Pointer Gestures | No multipoint/path gestures without alternative |
| 3.2.3 | Consistent Navigation | Navigation consistent across pages |
| 3.2.4 | Consistent Identification | Same function = same label across pages |
| 3.3.3 | Error Suggestion | Suggest correction when input error detected |
| 3.3.4 | Error Prevention (Legal/Financial) | Reversible, checked, or confirmed submissions |

## Common ARIA Patterns

### Dialog (Modal)

```html
<div role="dialog" aria-modal="true" aria-labelledby="dialog-title">
  <h2 id="dialog-title">Confirm Action</h2>
  <p>Are you sure you want to proceed?</p>
  <button>Confirm</button>
  <button>Cancel</button>
</div>
```

**Requirements:**
- `aria-modal="true"` traps focus inside
- Label via `aria-labelledby` or `aria-label`
- Focus moves to dialog on open, returns to trigger on close
- `Escape` key closes dialog

### Tabs

```html
<div role="tablist" aria-label="Settings">
  <button role="tab" aria-selected="true" aria-controls="panel-1" id="tab-1">General</button>
  <button role="tab" aria-selected="false" aria-controls="panel-2" id="tab-2" tabindex="-1">Privacy</button>
</div>
<div role="tabpanel" id="panel-1" aria-labelledby="tab-1">...</div>
<div role="tabpanel" id="panel-2" aria-labelledby="tab-2" hidden>...</div>
```

**Keyboard:** `Arrow Left/Right` to switch tabs, `Tab` to enter panel.

### Disclosure (Accordion)

```html
<button aria-expanded="false" aria-controls="content-1">Section Title</button>
<div id="content-1" hidden>Content here...</div>
```

### Live Region

```html
<!-- Polite: announced after current speech -->
<div aria-live="polite" aria-atomic="true">3 items in cart</div>

<!-- Assertive: interrupts current speech -->
<div role="alert">Error: Invalid email address</div>

<!-- Status: polite + role=status -->
<div role="status">File uploaded successfully</div>
```

## Screen Reader Testing Commands

### VoiceOver (macOS)

| Action | Shortcut |
|--------|----------|
| Toggle VoiceOver | `Cmd + F5` |
| Next element | `VO + Right Arrow` (`VO` = `Ctrl + Option`) |
| Activate element | `VO + Space` |
| Read headings | `VO + Cmd + H` |
| Open rotor | `VO + U` |

### NVDA (Windows)

| Action | Shortcut |
|--------|----------|
| Next element | `Tab` / `Down Arrow` |
| Headings list | `Insert + F7` |
| Next heading | `H` |
| Next landmark | `D` |
| Elements list | `Insert + F7` |

## Audit Checklist

### Images & Media
- [ ] Every `<img>` has `alt` attribute
- [ ] Decorative images use `alt=""` and/or `role="presentation"`
- [ ] Complex images have extended description
- [ ] Video has captions, audio has transcript

### Structure
- [ ] One `<h1>` per page, no skipped heading levels
- [ ] Landmark roles: `<header>`, `<nav>`, `<main>`, `<footer>`
- [ ] Skip navigation link present
- [ ] Page has descriptive `<title>`
- [ ] `<html lang="...">` set correctly

### Interactive Elements
- [ ] All buttons have accessible name
- [ ] All links have descriptive text (not "click here")
- [ ] Custom widgets have correct ARIA role + states
- [ ] `aria-expanded`, `aria-selected`, `aria-checked` updated dynamically

### Keyboard
- [ ] All interactive elements reachable via `Tab`
- [ ] Focus indicator visible on all focusable elements
- [ ] No keyboard traps
- [ ] Modal dialogs trap focus correctly
- [ ] `Escape` closes modals/popups

### Forms
- [ ] Every input has associated `<label>` or `aria-label`
- [ ] Required fields marked with `aria-required="true"`
- [ ] Error messages linked with `aria-describedby`
- [ ] Form errors announced via `role="alert"` or `aria-live`

### Color & Contrast
- [ ] Text contrast >= 4.5:1 (3:1 for large text)
- [ ] UI component contrast >= 3:1
- [ ] No information conveyed by color alone
- [ ] Respects `prefers-color-scheme` and `prefers-reduced-motion`
