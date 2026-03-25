# Consistency Checklist

Run this checklist before completing any module integration work.

## Design System Compliance

- [ ] All components imported from your design system
- [ ] No hardcoded colors (use semantic tokens: `bg-base-*`, `text-base-*`)
- [ ] No hardcoded spacing (use numeric scale: `p-200`, `gap-100`)
- [ ] No hardcoded border-radius (use token scale: `rounded-200`)
- [ ] All components have `theme-ui-label` attribute where applicable
- [ ] Style customizations in `-styles.ts` files, not inline
- [ ] Using `styling` prop (not `variant`) for component variants
- [ ] Using `size` prop (not `sz`) for component sizes

## Module Structure

- [ ] Files in correct `_` directories (`_components/`, `_hooks/`, etc.)
- [ ] Data flow follows Component -> Hook -> Action -> Service pattern
- [ ] Redux slice registered in root store
- [ ] TypeScript interfaces in `_ui-models/`
- [ ] No direct API calls from components

## TypeScript

- [ ] All files use `.ts`/`.tsx` extensions
- [ ] No `any` types (use proper typing)
- [ ] Interfaces exported for reuse
- [ ] Props interfaces defined for all components

## Accessibility

- [ ] Semantic HTML elements used (nav, main, section, article)
- [ ] ARIA labels on icon buttons
- [ ] Keyboard navigation works (Tab, Enter, Escape)
- [ ] Focus indicators visible
- [ ] Color contrast meets WCAG AA
- [ ] Form labels associated with inputs

## Internationalization

- [ ] All user-facing strings use i18n translation keys
- [ ] Translation keys in module namespace
- [ ] No hardcoded text in components

## Responsive

- [ ] Mobile-first design approach
- [ ] Tested at sm (640px), md (768px), lg (1024px) breakpoints
- [ ] No horizontal overflow on mobile

## Build

- [ ] Project build passes
- [ ] Project lint passes
- [ ] No TypeScript errors (`npx tsc --noEmit`)
