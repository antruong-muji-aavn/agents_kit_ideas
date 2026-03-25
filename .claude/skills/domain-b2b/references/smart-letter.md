---
module: smart-letter
last-verified: 2026-02-11
source-path: apps/luz-epost/app/[locale]/(auth)/smart-letter
---

# Smart Letter Module

## Purpose

Letter composition and rendering for physical mail delivery. Part of the SmartLetter Go! challenge (Feb 3 - March 16, 2026). Enables users to compose, preview, and send physical letters through the ePost platform.

**Status**: New module — under active development.

## Component Inventory

> To be populated as module is built

| Component | File | Key Props | Description |
|-----------|------|-----------|-------------|
| _New module_ | _in development_ | — | — |

## Hook Inventory

| Hook | File | Returns | Used By |
|------|------|---------|---------|
| _New module_ | _in development_ | — | — |

## Store Shape (Redux)

> To be designed as module is built

## API Endpoints

> To be defined as backend APIs are created

| Endpoint | Method | Request | Response | Service File |
|----------|--------|---------|----------|-------------|
| _New module_ | — | — | — | — |

## Expected Architecture

Based on existing module patterns:

```
smart-letter/
  _components/
    LetterComposer.tsx      # Main composition editor
    LetterPreview.tsx        # Letter preview (physical layout)
    LetterTemplateList.tsx   # Template selection
    RecipientSelector.tsx    # Recipient from contacts
  _hooks/
    useLetterComposer.ts    # Composition state
    useLetterTemplates.ts   # Template CRUD
  _actions/
    letterActions.ts        # Redux actions
  _stores/
    letterSlice.ts          # Redux slice
  _services/
    letterService.ts        # API calls
  _ui-models/
    letter.ts               # TypeScript interfaces
  page.tsx
  layout.tsx
```

## Cross-Module Dependencies

| Dependency | Direction | Purpose |
|------------|-----------|---------|
| Contacts | imports from | Recipient selection |
| Composer | imports from | Text editing patterns |
| Smart Send | related | Delivery integration |

## Known Patterns & Conventions

- Follow existing module file structure exactly
- Use klara-theme components for all UI
- Follow Component -> Hook -> Action -> Service -> API pattern
- i18n keys in module namespace
- Physical letter preview requires special rendering
