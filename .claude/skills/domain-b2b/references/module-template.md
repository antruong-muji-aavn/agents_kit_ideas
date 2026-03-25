# Module Knowledge Template

Use this template when creating or updating module reference files.

```markdown
---
module: {module-slug}
last-verified: {YYYY-MM-DD}
source-path: apps/luz-epost/app/[locale]/(auth)/{module-path}
---

# {Module Name} Module

## Purpose
{2-3 sentence business description}

## Component Inventory

| Component | File | Key Props | Description |
|-----------|------|-----------|-------------|
| {Name} | {path} | {props} | {desc} |

## Hook Inventory

| Hook | File | Returns | Used By |
|------|------|---------|---------|
| {useName} | {path} | {type} | {components} |

## Store Shape (Redux)

**Slice**: `{sliceName}`

| Field | Type | Description |
|-------|------|-------------|
| {field} | {type} | {desc} |

**Key Selectors**:
- `select{Field}` — {description}

## API Endpoints

| Endpoint | Method | Request | Response | Service File |
|----------|--------|---------|----------|-------------|
| {path} | GET/POST | {type} | {type} | {file} |

## Data Flow

```
Component ({name})
  -> Hook ({useName})
    -> Action ({actionName})
      -> Service ({serviceName})
        -> Caller ({callerMethod})
          -> Backend API ({endpoint})
```

## Cross-Module Dependencies

| Dependency | Direction | Purpose |
|------------|-----------|---------|
| {module} | imports from / exports to | {why} |

## Known Patterns & Conventions

- {Pattern 1}
- {Pattern 2}

## File Structure

```
app/[locale]/(auth)/{module-path}/
  _components/
  _actions/
  _stores/
  _hooks/
  _services/
  _ui-models/
  _constants/
  _enums/
  _utils/
  page.tsx
  layout.tsx
```
```
