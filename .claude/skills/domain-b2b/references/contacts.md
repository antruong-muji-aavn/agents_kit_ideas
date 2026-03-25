---
module: contacts
last-verified: 2026-02-11
source-path: apps/luz-epost/app/[locale]/(auth)/contacts
---

# Contacts Module

## Purpose

Contact management with import/export, grouping, and search. Central address book shared across messaging modules for recipient selection.

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
| Composer | exports to | Recipient selection |
| Smart Send | exports to | Audience targeting |
| Communities | exports to | Member management |

## Known Patterns & Conventions

- CRUD with search and filtering
- CSV/Excel import/export
- Contact grouping and tagging
- Deduplication handling
- Shared across all messaging modules
