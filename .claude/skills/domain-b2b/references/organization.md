---
module: organization
last-verified: 2026-02-11
source-path: apps/luz-epost/app/[locale]/(auth)/organization
---

# Organization Module

## Purpose

Organization-level settings, user management, roles, and permissions. Controls access across all B2B modules and manages organization profile.

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
| All modules | exports to | Permissions, user context, org settings |

## Known Patterns & Conventions

- Role-based access control (RBAC)
- User invitation and management
- Organization profile and branding
- Subscription and billing settings
- Audit logging
