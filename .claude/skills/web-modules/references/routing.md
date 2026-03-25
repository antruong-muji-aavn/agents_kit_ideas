# Module Routing — Next.js App Router

## Route Structure

```
app/
  [locale]/
    (auth)/                         # Auth-required layout group
      module-name/
        page.tsx                    # /module-name (list view)
        layout.tsx                  # Module layout wrapper
        [id]/
          page.tsx                  # /module-name/:id (detail view)
          edit/
            page.tsx                # /module-name/:id/edit
        create/
          page.tsx                  # /module-name/create
```

## Page Component Pattern

```tsx
// page.tsx (Server Component by default)
import { LetterList } from './_components/LetterList';

export default function SmartLetterPage() {
  return (
    <div className="p-300">
      <h1 className="text-2xl font-semibold text-base-foreground mb-200">
        Smart Letter
      </h1>
      <LetterList />
    </div>
  );
}
```

## Layout Pattern

```tsx
// layout.tsx
export default function SmartLetterLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col h-full">
      {/* Module-level navigation/tabs if needed */}
      <main className="flex-1 overflow-auto">
        {children}
      </main>
    </div>
  );
}
```

## Dynamic Routes

```tsx
// [id]/page.tsx
import { LetterDetail } from '../_components/LetterDetail';

export default function LetterDetailPage({
  params,
}: {
  params: { id: string };
}) {
  return <LetterDetail letterId={params.id} />;
}
```

## Navigation

```tsx
import { useRouter } from '@/navigation'; // your app's navigation.ts wrapper
import { useParams } from 'next/navigation';

// Navigate programmatically
const router = useRouter();
router.push('/smart-letter/create');
router.push(`/smart-letter/${id}`);
router.back();

// Read current params
const { id } = useParams();
const { locale } = useParams();
```

## Conventions

- `page.tsx` is a Server Component by default
- Interactive parts go in `_components/` as Client Components (`'use client'`)
- Module-private directories use `_` prefix (not accessible as routes)
- Layout wraps all child routes
- Use `(group)` syntax for layout groups without URL segments
