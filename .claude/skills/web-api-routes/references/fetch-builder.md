# FetchBuilder Complete Reference

## File Location

`service/fetch-builder.ts`

## Class: `FetchBuilder<T>`

Generic class with fluent chainable API. Internal state holds URL, method, headers, body, cache, tags, auth, timeout, form data, and query params.

## All Chainable Methods

| Method | Signature | Notes |
|--------|-----------|-------|
| `withUrl` | `(url: string)` | Sets the request URL |
| `withTenantId` | `(tenantId: string)` | Substitutes `:tenantId` via utility function |
| `withUsername` | `(username: string)` | Substitutes `:username` via `.replace()` |
| `withWorkplaceId` | `(workplaceId: string)` | Substitutes `:workplaceId` via utility function |
| `withMethod` | `('GET' \| 'POST' \| 'PUT' \| 'DELETE' \| 'PATCH')` | Default: `'GET'` |
| `withBearerToken` | `(token: string)` | Sets `Authorization: Bearer {token}` |
| `withTokenAuth` | `(token: string)` | Alias for `withBearerToken` |
| `withJsonBody<V>` | `(body: V)` | Sets body + `Content-Type: application/json` |
| `withHeaders` | `(headers: Record<string, string>)` | Merges into existing headers |
| `withCache` | `(cache: RequestCache)` | Sets fetch `cache` option |
| `withTags` | `(tags: string[])` | Next.js revalidation tags |
| `withTimeout` | `(timeout: number)` | Abort after N ms |
| `withParams` | `(params: Record<string, string>)` | Appends as query string |
| `withFormData` | `(formData: FormData)` | Sends multipart; clears body |
| `withFormDataFromObject` | `(data: Record<string, string>)` | Builds FormData from object |
| `withFormUrlencodedBody` | `(data: Record<string, string>)` | `application/x-www-form-urlencoded` |
| `execute` | `()` | Returns `Promise<FetchResponse<T>>` |

## Execute Behavior

1. Applies `Authorization: Bearer` if token is set
2. Substitutes `:tenantId`, `:username`, `:workplaceId` in URL
3. Appends query params
4. For DELETE responses: returns `{ result: {} as T }` (no body parsing)
5. Parses JSON if `Content-Type: application/json`, otherwise returns text
6. On non-ok response: returns `{ error: { status, reason } }`
7. Includes `tenantId` in all return shapes when set

## Error Handling

FetchBuilder **never throws**. Always check the response:

```typescript
const response = await new FetchBuilder<User>()
  .withUrl(API.GET_USER)
  .withTenantId(tenantId)
  .withBearerToken(token)
  .execute();

// Pattern 1: Return error directly
if (response.error) return response.error;
return response.result!;

// Pattern 2: Log and return null
if (response.error || !response.result) {
  logger.error(`[Caller] Failed: ${response.error?.reason}`);
  return null;
}

// Pattern 3: Use isErrorResponse type guard
if (isErrorResponse(response)) {
  // handle error
}
```

## Waterfall Prevention

See `web-nextjs/references/performance.md` for parallel fetch patterns and waterfall prevention.

## URL Placeholder Utility Functions

```typescript
// libs/utils/request.ts
export const withTenantId = (url: string, tenantId: string): string => {
  if (url.includes(':tenantId')) return url.replace(':tenantId', tenantId);
  return url;
};

export const withWorkplaceId = (url: string, workplaceId: string): string => {
  if (url.includes(':workplaceId')) return url.replace(':workplaceId', workplaceId);
  return url;
};
```

## Cache Integration

```typescript
// Force-cache with revalidation tags
const response = await new FetchBuilder<Profile>()
  .withUrl(MY_SERVICE_API.USER_PROFILE)
  .withTenantId(tenantId)
  .withBearerToken(token)
  .withCache('force-cache')
  .withTags(['profile', tenantId])
  .execute();

// No cache (real-time data)
.withCache('no-cache')

// Revalidate on next request
.withCache('no-store')
```
