# Caller Patterns

## Location

All callers live in `caller/` and begin with `'use server'`.

## Pattern A: Explicit Params

Caller receives `tenantId` and `authToken` as arguments. Used when the calling context already has session data.

```typescript
// caller/profile-caller.ts
'use server';
import { MY_SERVICE_API } from '@your-app/utils';
import { FetchBuilder } from '../service/fetch-builder';

export const getUserProfile = async (tenantId: string, authToken: string) => {
  const response = await new FetchBuilder<Profile>()
    .withUrl(MY_SERVICE_API.USER_PROFILE)
    .withTenantId(tenantId)
    .withBearerToken(authToken)
    .withMethod('GET')
    .execute();

  if (response.error || !response.result) {
    logger.error(`[ProfileCaller] Failed to fetch profile for tenant: ${tenantId}`);
  }
  return response.result;
};
```

## Pattern B: Session Auto-Extracted

Caller extracts auth from the server session internally. No auth params needed from the caller's consumer. This is the **majority pattern**.

```typescript
// caller/user-caller.ts
'use server';
import { MY_SERVICE_API, ANOTHER_SERVICE_API } from '@your-app/utils';
import { retrieveSessionData } from '../service/session-service';
import { ErrorResponse, FetchBuilder } from '../service/fetch-builder';

export const updateUser = async (
  username: string,
  userData: UserData
): Promise<UserData | ErrorResponse> => {
  const { tenantId, authToken } = await retrieveSessionData();

  const response = await new FetchBuilder<UserData>()
    .withUrl(
      MY_SERVICE_API.UPDATE_USER
        .replace(':tenantId', tenantId)
        .replace(':username', username)
    )
    .withBearerToken(authToken)
    .withMethod('PUT')
    .withJsonBody(userData)
    .execute();

  if (response.error) return response.error;
  return response.result!;
};

export const sendInviteEmail = async (toEmail: string, language: string): Promise<void> => {
  const { tenantId, authToken, organizationName } = await retrieveSessionData();

  await new FetchBuilder<void>()
    .withUrl(ANOTHER_SERVICE_API.SEND_INVITE.replace(':tenantId', tenantId))
    .withBearerToken(authToken)
    .withParams({ toEmail, organizationName, language })
    .withMethod('POST')
    .execute();
};
```

## `retrieveSessionData` Definition

```typescript
// service/session-service.ts
export const retrieveSessionData = async () => {
  const session: ExtendedSession | null = await getServerSessionData();
  return {
    tenantId: session?.organizationId,
    authToken: session?.accessToken,
    organizationName: session?.organizationName,
    username: session?.user?.name ?? '',
    email: session?.user?.email,
    // Add other session fields your callers need
  };
};
```

There is also `getServerSessionData()` which returns the full `ExtendedSession` directly.

## When to Use Which Pattern

| Pattern | When | Example |
|---------|------|---------|
| Explicit params | Called from other callers or services that already have session | `getUserProfile(tenantId, token)` |
| Session auto-extracted | Called directly from Server Components or client actions | `updateUser(username, userData)` |

## Creating a New Caller

1. Create file in `caller/` with `'use server'` directive
2. Import API constant from your shared utils package
3. Import `FetchBuilder` from `../service/fetch-builder`
4. Choose pattern A or B based on caller context
5. Use `.replace()` for non-standard placeholders before `.withUrl()`
6. Always check `response.error` — FetchBuilder never throws
7. Return `response.result` or `response.error`
