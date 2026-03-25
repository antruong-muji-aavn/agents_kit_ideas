# Routing — Next.js App Router

## Route Groups

| Group | Purpose | Auth Required |
|-------|---------|---------------|
| `(auth)` | All authenticated features | Yes — redirects to login |
| `(public)` | Onboarding, public pages | No — bypasses auth middleware |

## Dynamic Segments

| Segment | Location | Purpose |
|---------|----------|---------|
| `[locale]` | Root | Language prefix |
| `[id]` | Feature routes | Resource identifier |
| `[category]` | List views | Category filter |
| `[detailId]` | Detail views | Detail item view |
| `[...rest]` | `(auth)/` | Catch-all → `notFound()` |

## `_prefix` Convention

Folders prefixed with `_` are excluded from routing by Next.js. Used for co-located non-route files:

| Folder | Content |
|--------|---------|
| `_components/` | Feature-specific UI components |
| `_services/` | Server-side service logic |
| `_actions/` | Server actions |
| `_hooks/` | Custom React hooks |
| `_stores/` | Redux slices and store config |
| `_utils/` | Utility functions |
| `_ui-models/` | TypeScript interfaces/types |
| `_enums/` | Enum definitions |
| `_constants/` | Feature constants |
| `_features/` | Sub-feature groupings |

## Layout Nesting

```
[locale]/layout.tsx         → ReduxProvider > AuthProvider > NextIntlClientProvider
  (auth)/layout.tsx         → Sidebar menu, session check, feature flag detection
    feature-a/layout.tsx    → Feature-specific providers
    feature-b/layout.tsx    → Feature-specific data provider (Redux store)
    feature-c/layout.tsx    → Feature-specific layout
    feature-d/layout.tsx    → Feature navigation
```

## Page Templates

### Server Component Page (data fetching)
```typescript
import { getServerSessionData } from '../utils/sessions-server';

export default async function FeaturePage() {
  const session = await getServerSessionData();
  const data = await fetchFeatureData(session.tenant.id, session.tenant.token);
  return <FeatureDashboard data={data} />;
}
```

### Client Component Page (interactive)
```typescript
'use client';
export default function FeaturePage() {
  // Uses hooks, Redux, event handlers
  return <FeatureView />;
}
```
