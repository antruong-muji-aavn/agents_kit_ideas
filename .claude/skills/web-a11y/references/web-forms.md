---
name: web-forms
description: Form accessibility rules — label association, error announcements, required indicators, fieldset/legend, autocomplete
---

# Form Accessibility

## Purpose

Rules for accessible forms: every input must have a programmatically associated label, errors must be announced without relying on colour alone, and required fields must be clearly indicated.

## Table of Contents

- [Label Association](#label-association)
- [Error Announcements](#error-announcements)
- [Required Fields](#required-fields)
- [Grouped Controls](#grouped-controls)
- [Autocomplete](#autocomplete)
- [Input Types](#input-types)

## Related Documents

- [web-aria](./web-aria.md) — aria-describedby, aria-invalid, role="alert"
- [web-contrast](./web-contrast.md) — Error colour contrast
- [web-semantic-html](./web-semantic-html.md) — Native form elements

---

## Label Association

Every form control must have a programmatically determined label. There are three valid methods.

### Method 1: for/id (Recommended)

```html
<!-- ✅ Explicit label with for/id -->
<label for="username">Username</label>
<input id="username" type="text" name="username" autocomplete="username" />
```

### Method 2: Implicit Wrapping

```html
<!-- ✅ Input wrapped inside label -->
<label>
  Email address
  <input type="email" name="email" autocomplete="email" />
</label>
```

### Method 3: aria-labelledby / aria-label

Use when no visible label can be rendered adjacent to the input.

```html
<!-- ✅ aria-label for standalone search -->
<input
  type="search"
  aria-label="Search products"
  placeholder="Search..."
/>

<!-- ✅ aria-labelledby for table-cell inputs -->
<table>
  <thead>
    <tr>
      <th id="col-qty">Quantity</th>
      <th id="col-price">Price</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <input type="number" aria-labelledby="col-qty" />
      </td>
      <td>
        <input type="number" aria-labelledby="col-price" />
      </td>
    </tr>
  </tbody>
</table>
```

### Anti-Patterns

```html
<!-- ❌ Placeholder as label — disappears on input, fails WCAG 1.3.1 -->
<input type="email" placeholder="Enter your email" />

<!-- ❌ Visual proximity only — no programmatic association -->
<p>First name</p>
<input type="text" name="first" />
```

```jsx
// React — always associate label with input
function FormField({ id, label, type = 'text', ...props }) {
  return (
    <div>
      <label htmlFor={id}>{label}</label>
      <input id={id} type={type} {...props} />
    </div>
  );
}
```

---

## Error Announcements

Errors must be communicated without relying on colour alone and must be announced to screen reader users.

### Inline Error Pattern

```html
<!-- ✅ Error linked via aria-describedby, field marked aria-invalid -->
<label for="email">Email address</label>
<input
  id="email"
  type="email"
  aria-invalid="true"
  aria-describedby="email-error"
  value="not-an-email"
/>
<p id="email-error" class="error-msg">
  <!-- Icon supplements but doesn't replace text -->
  <span aria-hidden="true">⚠</span>
  Please enter a valid email address (example@domain.com).
</p>
```

```css
/* ✅ Error indicator does not rely on colour alone */
.error-msg {
  color: #b91c1c; /* red — but also has icon and text */
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.875rem;
}

input[aria-invalid="true"] {
  border-color: #b91c1c;
  /* Plus a non-colour indicator: thicker border, icon */
  border-width: 2px;
  padding-right: 32px;
  background: url('/icons/error.svg') right 8px center no-repeat;
}
```

### Summary Error Pattern (for Complex Forms)

```html
<!-- ✅ Error summary at top of form, announced on submit -->
<div
  id="error-summary"
  role="alert"
  aria-labelledby="error-summary-title"
  tabindex="-1"
>
  <h2 id="error-summary-title">There are 2 errors in this form</h2>
  <ul>
    <li><a href="#email">Email: Please enter a valid email address</a></li>
    <li><a href="#phone">Phone: Phone number is required</a></li>
  </ul>
</div>
```

```js
// ✅ On form submit: focus the error summary
form.addEventListener('submit', (e) => {
  const errors = validate(form);
  if (errors.length > 0) {
    e.preventDefault();
    renderErrors(errors);
    document.getElementById('error-summary').focus();
  }
});
```

```jsx
// React form error handling
function ContactForm() {
  const [errors, setErrors] = useState({});
  const errorSummaryRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    const validation = validateForm(formData);
    if (Object.keys(validation).length > 0) {
      setErrors(validation);
      // Focus summary after state update
      setTimeout(() => errorSummaryRef.current?.focus(), 0);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      {Object.keys(errors).length > 0 && (
        <div role="alert" tabIndex={-1} ref={errorSummaryRef}>
          <h2>Please fix the following errors:</h2>
          <ul>
            {Object.entries(errors).map(([field, msg]) => (
              <li key={field}>
                <a href={`#${field}`}>{msg}</a>
              </li>
            ))}
          </ul>
        </div>
      )}

      <label htmlFor="email">Email address</label>
      <input
        id="email"
        type="email"
        aria-invalid={!!errors.email}
        aria-describedby={errors.email ? 'email-error' : undefined}
      />
      {errors.email && (
        <p id="email-error" role="alert">
          {errors.email}
        </p>
      )}
    </form>
  );
}
```

---

## Required Fields

Required fields must be indicated without relying on colour alone.

```html
<!-- ✅ required attribute + visible indicator -->
<label for="name">
  Full name
  <span aria-hidden="true"> *</span>
  <span class="sr-only"> (required)</span>
</label>
<input id="name" type="text" required aria-required="true" />

<!-- ✅ Note at top of form explaining the indicator -->
<p>Fields marked with <span aria-hidden="true">*</span> are required.</p>
```

```css
/* Visually-hidden class for screen reader only text */
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
```

```jsx
// React required field with accessible indicator
function RequiredField({ id, label, type = 'text', ...props }) {
  return (
    <div>
      <label htmlFor={id}>
        {label}
        <span aria-hidden="true"> *</span>
        <span className="sr-only"> (required)</span>
      </label>
      <input
        id={id}
        type={type}
        required
        aria-required="true"
        {...props}
      />
    </div>
  );
}
```

---

## Grouped Controls

Related controls must be grouped with `<fieldset>` and `<legend>`.

```html
<!-- ✅ Radio group with fieldset/legend -->
<fieldset>
  <legend>Preferred contact method</legend>
  <label>
    <input type="radio" name="contact" value="email" /> Email
  </label>
  <label>
    <input type="radio" name="contact" value="phone" /> Phone
  </label>
  <label>
    <input type="radio" name="contact" value="post" /> Post
  </label>
</fieldset>

<!-- ✅ Checkbox group -->
<fieldset>
  <legend>Notification preferences</legend>
  <label>
    <input type="checkbox" name="notif-email" /> Email notifications
  </label>
  <label>
    <input type="checkbox" name="notif-sms" /> SMS notifications
  </label>
</fieldset>

<!-- ✅ Date group using ARIA when fieldset isn't suitable -->
<div role="group" aria-labelledby="dob-label">
  <p id="dob-label">Date of birth</p>
  <label for="dob-day">Day</label>
  <input id="dob-day" type="number" min="1" max="31" />
  <label for="dob-month">Month</label>
  <input id="dob-month" type="number" min="1" max="12" />
  <label for="dob-year">Year</label>
  <input id="dob-year" type="number" min="1900" max="2099" />
</div>
```

---

## Autocomplete

The `autocomplete` attribute helps users fill forms faster and reduces errors. Required by WCAG 2.1 SC 1.3.5 for fields that collect personal information.

```html
<!-- ✅ Common autocomplete tokens -->
<input type="text"     autocomplete="name"            />  <!-- Full name -->
<input type="text"     autocomplete="given-name"      />  <!-- First name -->
<input type="text"     autocomplete="family-name"     />  <!-- Last name -->
<input type="email"    autocomplete="email"           />
<input type="tel"      autocomplete="tel"             />
<input type="text"     autocomplete="organization"    />
<input type="text"     autocomplete="street-address"  />
<input type="text"     autocomplete="address-level2"  />  <!-- City -->
<input type="text"     autocomplete="postal-code"     />
<input type="text"     autocomplete="country-name"    />
<input type="password" autocomplete="current-password"/>
<input type="password" autocomplete="new-password"   />
<input type="text"     autocomplete="username"        />
<input type="text"     autocomplete="cc-name"         />  <!-- Card name -->
<input type="text"     autocomplete="cc-number"       />  <!-- Card number -->

<!-- ✅ Opt out only for security-sensitive custom fields -->
<input type="text" autocomplete="off" />
```

---

## Input Types

Use the correct `type` attribute to invoke appropriate mobile keyboards and browser validation.

| Type | Use Case | Mobile Keyboard |
|------|----------|-----------------|
| `text` | General free text | Standard |
| `email` | Email addresses | Email (`@` visible) |
| `tel` | Phone numbers | Numeric + symbols |
| `number` | Numeric values | Numeric |
| `url` | Web addresses | URL (`/` `.` visible) |
| `search` | Search inputs | Standard + search action |
| `password` | Passwords | Hidden characters |
| `date` | Date picker | Native date UI |
| `checkbox` | Boolean toggle | N/A |
| `radio` | Single choice from group | N/A |

### Checklist

- [ ] Every `<input>`, `<select>`, `<textarea>` has an associated `<label>`
- [ ] No placeholder-only labels
- [ ] `aria-invalid="true"` set on invalid fields
- [ ] Error messages linked via `aria-describedby`
- [ ] Error messages do not rely on colour alone
- [ ] Required fields use `required` + visible indicator
- [ ] Radio/checkbox groups wrapped in `<fieldset><legend>`
- [ ] Personal information fields include `autocomplete` tokens
- [ ] Correct `type` attribute on all inputs
