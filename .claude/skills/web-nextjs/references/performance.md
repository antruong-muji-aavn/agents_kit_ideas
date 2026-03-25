# Performance Patterns

Best practices from Vercel, adapted for Next.js 14 App Router applications.

## Parallel Fetching

**Problem**: Sequential `await` calls create request waterfalls.

```typescript
// BAD — sequential waterfall
const profile = await getUserProfile(tenantId, token);
const settings = await getSettings(tenantId, token);
const contacts = await getContacts(tenantId, token);

// GOOD — parallel with Promise.all
const [profile, settings, contacts] = await Promise.all([
  getUserProfile(tenantId, token),
  getSettings(tenantId, token),
  getContacts(tenantId, token),
]);
```

Apply to Server Components that call multiple callers at the page level.

## Deferred Await

Move `await` into the branch where data is actually used:

```typescript
// BAD — awaits immediately even if not needed
const data = await fetchData(tenantId, token);
if (condition) return <Fallback />;
return <Component data={data} />;

// GOOD — defer await until needed
const dataPromise = fetchData(tenantId, token);
if (condition) return <Fallback />;
const data = await dataPromise;
return <Component data={data} />;
```

## Suspense Boundaries

Wrap slow data-fetching sections in `<Suspense>` to avoid blocking the entire page:

```typescript
import { Suspense } from 'react';

export default async function Page() {
  return (
    <div>
      <FastSection />
      <Suspense fallback={<Skeleton />}>
        <SlowDataSection />  {/* This can stream in later */}
      </Suspense>
    </div>
  );
}
```

Use for: Slow API responses, large data tables, real-time feeds.

## Dynamic Imports

Use `next/dynamic` for heavy client components that aren't needed on initial render:

```typescript
import dynamic from 'next/dynamic';

const RichTextEditor = dynamic(
  () => import('./_components/rich-text-editor'),
  { loading: () => <EditorSkeleton /> }
);

const HeavyClientWidget = dynamic(
  () => import('./_components/heavy-client-widget'),
  { ssr: false }  // Browser-only — uses IndexedDB or Web APIs
);
```

Use for: Rich text editors, encryption setup UI, PDF viewers, chart components.

## Third-Party Script Loading

Load analytics and non-critical scripts after hydration:

```typescript
import Script from 'next/script';

<Script strategy="lazyOnload" src="https://analytics.example.com/script.js" />
```

## Memoization in Server Components

Use `React.cache()` to deduplicate data fetching across components in the same render:

```typescript
import { cache } from 'react';

const getProfile = cache(async (tenantId: string, token: string) => {
  return getUserProfile(tenantId, token);
});
```

## FetchBuilder Cache Integration

Use `withCache()` and `withTags()` for Next.js data cache:

```typescript
const response = await new FetchBuilder<Profile>()
  .withUrl(PROFILE_API.USER_PROFILE)
  .withTenantId(tenantId)
  .withBearerToken(token)
  .withCache('force-cache')
  .withTags(['profile', tenantId])
  .execute();
```

## Rules

- Fetch all independent data at the page level, pass down to components
- Never `await` sequentially when calls are independent
- Use `<Suspense>` for any section that takes >200ms to load
- Dynamic import components over 50KB that aren't above the fold
- Do NOT use `after()` (Next.js 15+) or `<Activity>` (React 19) — not available in this project
