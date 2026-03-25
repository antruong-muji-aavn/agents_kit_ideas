---
module: archive
last-verified: 2026-02-11
source-path: apps/luz-epost/app/[locale]/(auth)/archive
---

# Archive Module

## Purpose

Document archival, search, and retention management. Stores historical communications with full-text search, filtering, and compliance-oriented retention policies.

## Component Inventory

> Populate by scanning `_components/` directory

| Component | File | Key Props | Description |
|-----------|------|-----------|-------------|
| _TBD_ | _scan required_ | — | — |

## Hook Inventory

| Hook | File | Returns | Used By |
|------|------|---------|---------|
| _TBD_ | _scan required_ | — | — |

## Store Shape (Redux)

> Populate by scanning `_stores/` directory

## API Endpoints

| Endpoint | Method | Request | Response | Service File |
|----------|--------|---------|----------|-------------|
| _TBD_ | — | — | — | — |

## Cross-Module Dependencies

| Dependency | Direction | Purpose |
|------------|-----------|---------|
| Unified Inbox | imports from | Archived messages |

## Known Patterns & Conventions

- Full-text search with highlighting
- Date range and type filtering
- Retention policy management
- Export and download capabilities
- Pagination for large result sets
