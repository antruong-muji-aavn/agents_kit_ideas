# Middleware — Combined Auth + i18n

## Architecture

`middleware.ts` composes two middlewares: `intlMiddleware` (locale routing) wraps inside `authMiddleware` (next-auth session check).

```typescript
// intlMiddleware — handles locale detection and routing
const intlMiddleware = createIntlMiddleware({
  locales,               // ['en', ...] — your supported locales
  localePrefix: 'always',
  localeDetection: true,
  defaultLocale,         // your configured default
});

// authMiddleware — wraps next-auth withAuth, runs intl inside
const authMiddleware = withAuth(
  async function middleware(request: NextRequestWithAuth) {
    // Extract session data from JWT
    const sessionData = request.nextauth?.token?.customData;

    // Feature flag path guards
    const featureFlagPathHandlers = [
      { regex: /\/feature-a/, featureFlag: FEATURE_FLAGS.FEATURE_A_ENABLED },
      { regex: /\/feature-b/, featureFlag: FEATURE_FLAGS.FEATURE_B_ENABLED },
      { regex: /\/feature-c/, featureFlag: FEATURE_FLAGS.FEATURE_C_ENABLED },
    ];

    // Check restrictions: role checks, subscription checks
    const isEnabled = await isFeatureFlagEnabled(tenantToken, tenantId, username, featureFlag, traceId);

    if (!isEnabled) return NextResponse.redirect(notFoundUrl);

    return intlMiddleware(request); // hand off to intl if all checks pass
  },
  {
    callbacks: { authorized: ({ token }) => !!token },
    pages: { signIn: './api/auth/login' },
  }
);

// Combined entry point
export default function combinedMiddleware(req: NextRequest, event: NextFetchEvent) {
  if (isPublicUrl(req.nextUrl.pathname)) {
    return intlMiddleware(req); // public URLs bypass auth
  }
  return authMiddleware(req, event);
}
```

## Request Tracing

```typescript
// libs/utils/request-monitoring.ts
export const generateRequestId = (request: NextRequest): string => {
  const existingID = request.headers.get('X-Request-Id'); // set at proxy layer
  if (existingID) return existingID;
  const id = uuidv4();
  request.headers.set('X-Request-Id', id);
  return id;
};

// Response headers include X-Request-Id for end-to-end tracing
response.headers.set('X-Request-Id', requestId);

// Logger adds trace context
const fullTrace = `projects/${projectId}/traces/${traceId}`;
```

## Matcher Config

Excludes static assets, API routes, and Next.js internals:

```typescript
export const config = {
  matcher: [{
    source: '/((?!api|_next/static|_next/image|_next/cache|robots.txt|public|images|assets/icons/*|manifest.json|sw.js|favicon.ico|workbox-*|pdf.worker.min.js).*)',
  }],
};
```

## Public URL Detection

```typescript
// libs/utils/url-utils.ts
// PUBLIC_URLS = ['/onboarding', '/public-error-page']
const publicPathnameRegex = RegExp(
  `^(/(${LOCALES.join('|')}))?(${PUBLIC_URLS.flatMap(p => ...).join('|')})/?$`, 'i'
);
export const isPublicUrl = (url: string) => publicPathnameRegex.test(url);
```
