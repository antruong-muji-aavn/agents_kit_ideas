---
module: unified-inbox
last-verified: 2026-02-11
source-path: apps/luz-epost/app/[locale]/(auth)/unified-inbox
---

# Unified Inbox Module

## Purpose

Centralized message inbox aggregating communications across multiple channels (email, platform messages, notifications). Provides unified view with filtering, search, and bulk actions.

## Component Inventory

> Populate by scanning `apps/luz-epost/app/[locale]/(auth)/unified-inbox/_components/`

| Component | File | Key Props | Description |
|-----------|------|-----------|-------------|
| _TBD_ | _scan required_ | — | — |

## Hook Inventory

> Populate by scanning `_hooks/` directory

| Hook | File | Returns | Used By |
|------|------|---------|---------|
| _TBD_ | _scan required_ | — | — |

## Store Shape (Redux)

> Populate by scanning `_stores/` directory

## API Endpoints

> Populate by scanning `_services/` directory

| Endpoint | Method | Request | Response | Service File |
|----------|--------|---------|----------|-------------|
| _TBD_ | — | — | — | — |

## Data Flow

```
InboxList -> useInboxMessages -> fetchMessages -> inboxService -> inboxCaller -> /api/messages
```

## Cross-Module Dependencies

| Dependency | Direction | Purpose |
|------------|-----------|---------|
| Archive | exports to | Message archival |
| Composer | imports from | Reply/forward actions |

## Known Patterns & Conventions

- Uses virtualized list for performance
- Message threading support
- Real-time updates via polling/websocket
- Bulk selection and actions (archive, delete, mark read)
