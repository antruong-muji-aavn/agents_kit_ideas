---
name: knowledge/klara-theme/integration
description: "klara-theme integration patterns: theming, composition, state"
---

# klara-theme Integration

## Theme Provider Setup

```tsx
import { ThemeProvider } from '@org/design-system';

function App() {
  return (
    <ThemeProvider defaultTheme="light">
      <YourApp />
    </ThemeProvider>
  );
}
```

## Composition Patterns

### Compound Components
```tsx
<Card>
  <Card.Header>Title</Card.Header>
  <Card.Body>Content</Card.Body>
  <Card.Footer>
    <Button>Action</Button>
  </Card.Footer>
</Card>
```

### Slot Pattern
```tsx
<Modal
  trigger={<Button>Open</Button>}
  header="Confirm"
  footer={<Button onClick={onConfirm}>OK</Button>}
>
  Modal body content
</Modal>
```

## State Management

- Form state: Use your project's form library with klara-theme's `FormField`
- Toast state: Use klara-theme's `useToast()` hook
- Modal state: Use component's built-in `open` prop + `onOpenChange`

## Custom Theming

Override tokens via CSS custom properties:
```css
:root {
  --color-primary: #YOUR_BRAND_COLOR;
  --radius-md: 12px;
}
```

Or use the ThemeProvider's `theme` prop for programmatic theming.
