# Module Scaffold — File Creation Order

When adding a new feature to a B2B module, create files in this order. Each layer depends on the previous.

## Creation Order

```
1. _ui-models/     → TypeScript interfaces and enums
2. _constants/     → Constants and config values
3. _enums/         → Enum definitions
4. _services/      → API service layer
5. _actions/       → Redux async thunks
6. _stores/        → Redux slice with reducers
7. _hooks/         → Custom React hooks
8. _components/    → React components (UI)
9. page.tsx        → Route entry point
10. layout.tsx     → Route layout (if needed)
```

## Why This Order

- Types first: everything else depends on type definitions
- Services before actions: actions call services
- Actions before stores: store handles action results
- Stores before hooks: hooks read from store
- Hooks before components: components use hooks
- Components before page: page composes components

## Directory Structure

```
app/[locale]/(auth)/{module-name}/
  _ui-models/
    feature.ts              # Feature-specific types
    index.ts                # Re-exports
  _constants/
    feature-constants.ts    # Magic values, configs
  _enums/
    feature-status.ts       # Status enums
  _services/
    featureService.ts       # API calls
  _actions/
    featureActions.ts       # Redux thunks
  _stores/
    featureSlice.ts         # Redux slice
  _hooks/
    useFeature.ts           # Main feature hook
    useFeatureForm.ts       # Form-specific hook
  _components/
    FeatureList.tsx          # List view
    FeatureDetail.tsx        # Detail view
    FeatureForm.tsx          # Create/edit form
  _utils/
    featureHelpers.ts       # Pure utility functions
  page.tsx                  # Route entry
  layout.tsx                # Layout wrapper
```

## Naming Conventions

- Files: `camelCase` for services/actions/hooks, `PascalCase` for components
- Directories: `_prefixed` for module-private, no prefix for route segments
- Types: `PascalCase` interfaces, `UPPER_SNAKE` enums
- Hooks: `use` prefix (React convention)
- Services: `{feature}Service` suffix
- Actions: `{verb}{Feature}` pattern (fetchLetters, createLetter)
- Slices: `{feature}Slice` suffix
