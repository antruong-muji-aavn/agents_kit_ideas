---
module: communities
last-verified: 2026-02-11
source-path: apps/luz-epost/app/[locale]/(auth)/communities
---

# Communities Module

## Purpose

Community and organization group management. Allows creating, managing, and communicating with defined groups of contacts for targeted messaging campaigns.

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
| Contacts | imports from | Member management |
| Smart Send | exports to | Audience targeting |

## Known Patterns & Conventions

- CRUD operations for community groups
- Member list management with pagination
- Community-scoped messaging
