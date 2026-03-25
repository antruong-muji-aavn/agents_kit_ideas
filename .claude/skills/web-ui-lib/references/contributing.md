---
name: knowledge/klara-theme/contributing
description: "How to propose new components back to the MUJI team"
---

# Contributing to klara-theme

## Proposing New Components

1. **Check existing components** — Search knowledge/klara-theme/components.md first
2. **Create a proposal** — Open a GitHub issue with the `component-proposal` template
3. **Include**: Use case, API sketch, Figma link (if available), priority justification
4. **MUJI team reviews** — Weekly component review meeting (Tuesdays)
5. **Approval → implementation** — MUJI team builds, feature team provides feedback

## Extending Existing Components

- **New variant**: PR to klara-theme repo with variant addition
- **Bug fix**: PR with reproduction steps
- **Documentation**: PR to Storybook stories

## Do NOT

- Copy component source into your app (use the published package)
- Override component internals with `!important` CSS
- Create duplicate components without checking with MUJI
