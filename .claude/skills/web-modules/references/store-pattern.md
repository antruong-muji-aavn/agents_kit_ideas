# Redux Store Pattern

## Slice Template

```typescript
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { fetchItems, createItem, deleteItem } from '../_actions/itemActions';
import { Item } from '../_ui-models/item';

interface ItemState {
  items: Item[];
  selectedItem: Item | null;
  loading: boolean;
  error: string | null;
  filters: {
    search: string;
    status: string | null;
  };
}

const initialState: ItemState = {
  items: [],
  selectedItem: null,
  loading: false,
  error: null,
  filters: {
    search: '',
    status: null,
  },
};

export const itemSlice = createSlice({
  name: 'moduleName',
  initialState,
  reducers: {
    setSelectedItem(state, action: PayloadAction<Item | null>) {
      state.selectedItem = action.payload;
    },
    setSearchFilter(state, action: PayloadAction<string>) {
      state.filters.search = action.payload;
    },
    setStatusFilter(state, action: PayloadAction<string | null>) {
      state.filters.status = action.payload;
    },
    clearFilters(state) {
      state.filters = initialState.filters;
    },
  },
  extraReducers: (builder) => {
    // Fetch
    builder
      .addCase(fetchItems.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchItems.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchItems.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? 'Failed to fetch';
      });

    // Create
    builder
      .addCase(createItem.fulfilled, (state, action) => {
        state.items.push(action.payload);
      });

    // Delete
    builder
      .addCase(deleteItem.fulfilled, (state, action) => {
        state.items = state.items.filter(i => i.id !== action.meta.arg);
      });
  },
});

export const { setSelectedItem, setSearchFilter, setStatusFilter, clearFilters } = itemSlice.actions;
export default itemSlice.reducer;
```

## Selectors

```typescript
// _stores/itemSelectors.ts
import { RootState } from '@/store';

export const selectItems = (state: RootState) => state.moduleName.items;
export const selectLoading = (state: RootState) => state.moduleName.loading;
export const selectError = (state: RootState) => state.moduleName.error;
export const selectSelectedItem = (state: RootState) => state.moduleName.selectedItem;

// Derived selector with filtering
export const selectFilteredItems = (state: RootState) => {
  const { items, filters } = state.moduleName;
  return items.filter(item => {
    if (filters.search && !item.name.toLowerCase().includes(filters.search.toLowerCase())) {
      return false;
    }
    if (filters.status && item.status !== filters.status) {
      return false;
    }
    return true;
  });
};
```

## Registering the Slice

Add to the root store configuration (usually in `store/index.ts` or `store/rootReducer.ts`).

## Conventions

- Slice name matches module slug
- State shape is flat (avoid deep nesting)
- Use `createAsyncThunk` for all API calls
- Sync reducers for UI state (filters, selection)
- Async reducers in `extraReducers` for API state
- Always handle pending/fulfilled/rejected
