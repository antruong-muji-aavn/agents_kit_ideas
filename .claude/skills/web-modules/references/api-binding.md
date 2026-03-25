# API Binding Pattern

## Data Flow

```
Component (UI layer)
  -> Custom Hook (state + effects)
    -> Redux Action (async thunk)
      -> Caller (FetchBuilder HTTP client)
          -> Backend API
```

## Layer 1: Component

```tsx
'use client';
import { useLetters } from '../_hooks/useLetters';

export function LetterList() {
  const { letters, loading, error, refresh } = useLetters();
  // Render UI using data from hook
}
```

**Rules**: Components ONLY call hooks. Never call services or dispatch actions directly.

## Layer 2: Custom Hook

```typescript
import { useEffect, useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { fetchLetters, deleteLetter } from '../_actions/letterActions';

export function useLetters() {
  const dispatch = useAppDispatch();
  const { letters, loading, error } = useAppSelector(s => s.smartLetter);

  useEffect(() => {
    dispatch(fetchLetters());
  }, [dispatch]);

  const refresh = useCallback(() => {
    dispatch(fetchLetters());
  }, [dispatch]);

  const remove = useCallback((id: string) => {
    dispatch(deleteLetter(id));
  }, [dispatch]);

  return { letters, loading, error, refresh, remove };
}
```

**Rules**: Hooks dispatch actions and select from store. Never call services directly.

## Layer 3: Redux Action (Async Thunk)

```typescript
import { createAsyncThunk } from '@reduxjs/toolkit';
import { getLetters } from '../_callers/letter-caller';
import { Letter } from '../_ui-models/letter';

export const fetchLetters = createAsyncThunk<Letter[]>(
  'smartLetter/fetchLetters',
  async (_, { rejectWithValue }) => {
    try {
      return await getLetters();
    } catch (error) {
      return rejectWithValue(error);
    }
  }
);
```

**Rules**: Actions call services and handle errors. Return typed payloads.

## Layer 4: Caller (FetchBuilder)

```typescript
// _callers/letter-caller.ts
'use server';
import { fetchBuilder } from '@/service/fetch-builder';
import { LETTER_API } from '@/constants/api-urls';
import { Letter } from '../_ui-models/letter';

export async function getLetters(): Promise<Letter[]> {
  return fetchBuilder(LETTER_API.LIST)
    .fetch<Letter[]>();
}

export async function getLetterById(id: string): Promise<Letter> {
  return fetchBuilder(LETTER_API.DETAIL(id))
    .fetch<Letter>();
}

export async function createLetter(data: Partial<Letter>): Promise<Letter> {
  return fetchBuilder(LETTER_API.LIST)
    .withMethod('POST')
    .withBody(data)
    .fetch<Letter>();
}
```

**Rules**: Callers use FetchBuilder for all HTTP requests. Never use raw `fetch()`. Return typed data.

## Anti-Patterns

- Component calling `fetch()` directly
- Hook calling service without going through action
- Action containing business logic
- Service containing UI logic
- Mixing layers (component dispatching AND calling service)
